/* sbt -- Simple Build Tool
 * Copyright 2011 Mark Harrah
 * Copyright 2012 Johannes Rudolph
 *
 * This was basically copied from the sbt source code and then adapted to use
 * `sbtVersion in sbtPlugin`.
 */
package sbt

import complete.DefaultParsers._
import complete.Parser
import sbt.Keys._
import Project._

object SbtPluginCross
{
	final val Switch = "^^"
	final val Cross = "^"

	def switchParser(state: State): Parser[(String, String)] =
	{
		val knownVersions = Nil
		lazy val switchArgs = token(NotSpace.examples(knownVersions : _*)) ~ (token(Space ~> matched(state.combinedParser)) ?? "")
		lazy val nextSpaced = spacedFirst(Switch)
		token(Switch ~ OptSpace) flatMap { _ => switchArgs & nextSpaced }
	}
	def spacedFirst(name: String) = opOrIDSpaced(name) ~ any.+

	lazy val switchVersion = Command.arb(requireSession(switchParser)) { case (state, (version, command)) =>
		val x = Project.extract(state)
	  import x._
		println("Setting `sbtVersion in sbtPlugin` to " + version)
		val add = (sbtVersion in GlobalScope in sbtPlugin :== version) :: Nil
		val cleared = session.mergeSettings.filterNot( crossExclude )
		val newStructure = Load.reapply(cleared ++ add, structure)
		Project.setProject(session, newStructure, command :: state)
	}
	def crossExclude(s: Setting[_]): Boolean =
		s.key match {
			case ScopedKey(Scope(_, _, sbtPlugin.key, _), sbtVersion.key) => true
			case _ => false
		}

	def crossParser(state: State): Parser[String] =
		token(Cross <~ OptSpace) flatMap { _ => token(matched( state.combinedParser & spacedFirst(Cross) )) }

	lazy val crossBuild = Command.arb(requireSession(crossParser)) { (state, command) =>
		val x = Project.extract(state)
			import x._
		val versions = crossVersions(state)
		val current = CrossBuilding.pluginSbtVersion get structure.data map(Switch + " " + _) toList;
		if(versions.isEmpty) command :: state else versions.map(Switch + " " + _ + " " + command) ::: current ::: state
	}
	def crossVersions(state: State): Seq[String] =
	{
		val x = Project.extract(state)
    import x._
		CrossBuilding.crossSbtVersions in currentRef get structure.data getOrElse Nil
	}

	def requireSession[T](p: State => Parser[T]): State => Parser[T] = s =>
		if(s get sessionSettings isEmpty) failure("No project loaded") else p(s)
}