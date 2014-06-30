package org.specs2
package reporter

import org.junit.runner.Description
import scalaz.{Tree, TreeLoc}
import data.Trees._
import TreeLoc._
import Tree._
import data.Trees
import Trees._
import specification._
import core._
import process._
import text.Trim
import Trim._
import control.ExecutionOrigin
import specification.core.{NoText, Fragment}
import specification.create.DefaultFragmentFactory

trait JUnitDescriptions extends ExecutionOrigin {

  def createDescription(spec: SpecStructure): Description =
    createDescription(createTreeLoc(spec))

  def createDescription(treeLoc: TreeLoc[Description]): Description = {
    treeLoc.toTree.bottomUp {
      (description: Description, children: Stream[Description]) =>
        children.foreach {
          child => description.addChild(child)
        }
        description
    }.rootLabel
  }

  def createTreeLoc(spec: SpecStructure) =
    createDescriptionTree(spec).map(_._2)

  def createDescriptionTree(spec: SpecStructure): TreeLoc[(Fragment, Description)] = {
    val className = spec.specClassName
    val rootFragment = DefaultFragmentFactory.text(spec.header.simpleName)

    Levels.treeLocMap(spec.fragments)(keep).getOrElse(leaf(rootFragment).loc).root.setLabel(rootFragment).cojoin.map {
      current: TreeLoc[Fragment] =>
        val description =
        current.getLabel match {
          case f @ Fragment(d, e, _) if !e.isRunnable   => createDescription(className, suiteName = testName(d.show))
          case f @ Fragment(NoText, e, _) if e.mustJoin => createDescription(className, label = current.size.toString, suiteName = "step")
          case f @ Fragment(NoText, e, _)               => createDescription(className, label = current.size.toString, suiteName = "action")
          case f @ Fragment(d, e, _)                    => createDescription(className, label = current.size.toString, testName = testName(d.show, parentPath(current.parents.map(_._2))))
        }
        (current.getLabel, description)
    }
  }

  /** description for the beginning of the specification */
  def specDescription(spec: SpecStructure) =
    createDescription(spec.specClassName, suiteName = testName(spec.name))

  /** Map of each fragment to its description */
  def fragmentDescriptions(spec: SpecStructure): Map[Fragment, Description] =
    createDescriptionTree(spec).root.toTree.flatten.toMap

  /** filter out the fragments which don't need to be represented in the JUnit descriptions */
  def keep: Levels.Mapper = {
    case f @ Fragment(Text(t), e, _) if t.trim.isEmpty => None
    case f if Fragment.isFormatting(f)                    => None
    case f                                                => Some(f)
  }

  def createDescription(className: String, suiteName: String = "", testName: String = "", label: String = ""): Description = {
    val origin =
      if (isExecutedFromAnIDE && !label.isEmpty) label
      else className

    val description =
      if (testName.isEmpty) (if (suiteName.isEmpty) className else suiteName)
      else (sanitize(testName)+"("+origin+")")

    Description.createSuiteDescription(description)
  }

  import text.Trim._

  /** @return a seq containing the path of an example without the root name */
  def parentPath(parentNodes: Seq[Fragment]) =
    parentNodes.reverse.drop(1).map(_.description.show)

  /** @return a test name with no newlines */
  def testName(s: String, parentNodes: Seq[String] = Seq()): String = {
    (if (parentNodes.isEmpty || isExecutedFromAnIDE) "" else parentNodes.map(_.replace("\n", "")).mkString("", "::", "::")) +
      (if (isExecutedFromAnIDE) Trimmed(s).removeNewLines else Trimmed(s).trimNewLines)
  }


  /** @return replace () with [] because it cause display issues in JUnit plugins */
  private def sanitize(s: String) = {
    val trimmed = Trimmed(s).trimReplace("(" -> "[",  ")" -> "]")
    if (trimmed.isEmpty) " "
    else trimmed
  }

}

object JUnitDescriptions extends JUnitDescriptions
