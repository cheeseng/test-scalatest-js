package org.podval.tools.test

import sbt.testing.{Event, Fingerprint, Framework, Logger, Runner, SubclassFingerprint, SuiteSelector,
  Task, TaskDef}

object SbtTestFrameworkBench:
  def runClass(
    className: String,
    framework: Framework,
    fingerprint: Fingerprint
  ): Unit =
    val taskDef: TaskDef = TaskDef(
      className,
      fingerprint,
      true,
      Array(SuiteSelector())
    )

    val runner: Runner = framework.runner(
      Array.empty,
      Array.empty,
      getClass.getClassLoader
    )

    for task: Task <- runner.tasks(Array(taskDef)) do runTask(task)

    println(s"Summary:\n${runner.done}")

  private def runTask(task: Task): Unit =
    val taskDef: TaskDef = task.taskDef
    println(
      s"""runTask(
         |  fullyQualifiedName=${taskDef.fullyQualifiedName},
         |  fingerprint=${taskDef.fingerprint},
         |  explicitlySpecified=${taskDef.explicitlySpecified},
         |  selectors=${taskDef.selectors.mkString(", ")},
         |)
         |""".stripMargin
    )

    val nestedTasks: Array[Task] =
      try
        task.execute(
          handleEvent(_),
          Array(testLogger)
        )
      catch case throwable =>
        println(s"Throwable: $throwable")
        Array.empty

    for nestedTask: Task <- nestedTasks do runTask(nestedTask)

  private def handleEvent(event: Event): Unit =
    val throwable: Option[Throwable] = if event.throwable.isEmpty then None else Some(event.throwable.get)

    println(
      s"""handleEvent(
         |  fullyQualifiedName=${event.fullyQualifiedName}
         |  selector=${event.selector}
         |  status=${event.status}
         |  throwable=$throwable
         |)""".stripMargin
    )

  private def testLogger: Logger = new Logger:
    private def log(logLevel: String, message: String): Unit = println(s"$logLevel: $message")
    override def ansiCodesSupported: Boolean = true
    override def error(message: String): Unit = log("ERROR", message)
    override def warn (message: String): Unit = log("WARN" , message)
    override def info (message: String): Unit = log("INFO" , message)
    override def debug(message: String): Unit = log("DEBUG", message)
    override def trace(throwable: Throwable): Unit = log("TRACE", throwable.getMessage)

class Nesting extends org.scalatest.Suites(
  new Nested
)

class Nested extends org.scalatest.flatspec.AnyFlatSpec {
  "success" should "pass" in {}
}

object ScalaTestIssue2357:
  private val scalaTestSbtFramework: Framework = org.scalatest.tools.Framework()
  private val fingerprint: Fingerprint = scalaTestSbtFramework.fingerprints
    .filter(_.isInstanceOf[SubclassFingerprint]).head
  private def run(className: String): Unit = SbtTestFrameworkBench.runClass(
    className,
    scalaTestSbtFramework,
    fingerprint
  )

  def main(args: Array[String]): Unit =
    println("Running Nested - test cases in the Nested are executed:")
    run("org.podval.tools.test.Nested")
    println
    println("-----------------------")
    println("Running Nesting - test cases in the Nested are NOT executed:")
    run("org.podval.tools.test.Nesting")