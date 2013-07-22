/*
 * This file was copied almost verbatim from [1] to support cross version support for
 * sbt 0.11.x.
 *
 * Changes:
 *   - removed `private[...]` modifiers
 *
 * [1] https://raw.github.com/sbt/sbt/c0b1bb51e63841be63c810a961031529b7be0072/util/cross/src/main/input_sources/CrossVersionUtil.scala
 */

package net.virtualvoid.sbt.cross

object CrossVersionUtil
{
	val trueString = "true"
	val falseString = "false"
	val fullString = "full"
	val noneString = "none"
	val disabledString = "disabled"
	val binaryString = "binary"
	val TransitionScalaVersion = "2.10"
	val TransitionSbtVersion = "0.12"

	def isFull(s: String): Boolean = (s == trueString) || (s == fullString)
	def isDisabled(s: String): Boolean = (s == falseString) || (s == noneString) || (s == disabledString)
	def isBinary(s: String): Boolean = (s == binaryString)

	def isSbtApiCompatible(v: String): Boolean = sbtApiVersion(v).isDefined
	/** Returns sbt binary interface x.y API compatible with the given version string v. 
	 * RCs for x.y.0 are considered API compatible.
	 * Compatibile versions include 0.12.0-1 and 0.12.0-RC1 for Some(0, 12).
	 */
	def sbtApiVersion(v: String): Option[(Int, Int)] =
  {
		val ReleaseV = """(\d+)\.(\d+)\.(\d+)(-\d+)?""".r
		val CandidateV = """(\d+)\.(\d+)\.(\d+)(-RC\d+)""".r
		val NonReleaseV = """(\d+)\.(\d+)\.(\d+)(-\w+)""".r
		v match {
			case ReleaseV(x, y, z, ht)    => Some((x.toInt, y.toInt))
			case CandidateV(x, y, z, ht)  => Some((x.toInt, y.toInt))
			case NonReleaseV(x, y, z, ht) if z.toInt > 0 => Some((x.toInt, y.toInt))
			case _ => None
		}
	}
	def isScalaApiCompatible(v: String): Boolean = scalaApiVersion(v).isDefined
	/** Returns Scala binary interface x.y API compatible with the given version string v. 
	 * Compatibile versions include 2.10.0-1 and 2.10.1-M1 for Some(2, 10), but not 2.10.0-RC1.
	 */
	def scalaApiVersion(v: String): Option[(Int, Int)] =
	{
		val ReleaseV = """(\d+)\.(\d+)\.(\d+)(-\d+)?""".r
		val NonReleaseV = """(\d+)\.(\d+)\.(\d+)(-\w+)""".r
		v match {
			case ReleaseV(x, y, z, ht)    => Some((x.toInt, y.toInt))
			case NonReleaseV(x, y, z, ht) if z.toInt > 0 => Some((x.toInt, y.toInt))
			case _ => None
		}
	}
	val PartialVersion = """(\d+)\.(\d+)(?:\..+)?""".r
	def partialVersion(s: String): Option[(Int,Int)] =
		s match {
			case PartialVersion(major, minor) => Some((major.toInt, minor.toInt))
			case _ => None
		}
	def binaryScalaVersion(full: String): String = binaryVersionWithApi(full, TransitionScalaVersion)(scalaApiVersion)
	def binarySbtVersion(full: String): String = binaryVersionWithApi(full, TransitionSbtVersion)(sbtApiVersion)
	def binaryVersion(full: String, cutoff: String): String = binaryVersionWithApi(full, cutoff)(scalaApiVersion)
	private[this] def isNewer(major: Int, minor: Int, minMajor: Int, minMinor: Int): Boolean =
		major > minMajor || (major == minMajor && minor >= minMinor)
	private[this] def binaryVersionWithApi(full: String, cutoff: String)(apiVersion: String => Option[(Int,Int)]): String =
	{
		def sub(major: Int, minor: Int) = major + "." + minor
		(apiVersion(full), partialVersion(cutoff)) match {
			case (Some((major, minor)), None) => sub(major, minor)
			case (Some((major, minor)), Some((minMajor, minMinor))) if isNewer(major, minor, minMajor, minMinor) => sub(major, minor)
			case _ => full
		}
	}
}
