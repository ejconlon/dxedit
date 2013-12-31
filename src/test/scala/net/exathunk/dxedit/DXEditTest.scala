package net.exathunk.dxedit

import org.scalatest.FunSuite

class DXEditTest extends FunSuite {

  // Matcher tests

  import ImplicitIntToByte._
  import ByteMatchers._

  def assertMatchEquals(gold: Byte, test: Byte) {
    assert(Some(gold) == matchEquals(gold)(test))
  }
  def assertNotMatchEquals(gold: Byte, test: Byte) {
    assert(None == matchEquals(gold)(test))
  }
  def assertMatchAny(test: Byte) {
    assert(Some(test) == matchAny(test))
  }
  def assertMatchLike(gold: Byte, pattern: String, test: Byte) {
    assert(Some(gold) == matchLike(pattern)(test))
  }
  def assertNotMatchLike(pattern: String, test: Byte) {
    assert(None == matchLike(pattern)(test))
  }
  def assertMatchSeven(test: Byte) {
    assert(Some(test) == matchSeven(test))
  }
  def assertNotMatchSeven(test: Byte) {
    assert(None == matchSeven(test))
  }

  test("matchEquals") {
    assertMatchEquals(3, 3)
    assertNotMatchEquals(3, 4)
  }

  test("matchAny") {
    assertMatchAny(3)
  }

  test("matchLike") {
    assertMatchLike(0xF, "1111vvvv", 0xFF)
    assertNotMatchLike("1111vvvv", 0xF)
    assertMatchLike(0xF, "0000vvvv", 0xF)
    assertNotMatchLike("0000vvvv", 0xFF)
    assertMatchLike(0xF0, "vvvv0000", 0xF0)
    assertNotMatchLike("vvvv0000", 0xFF)
    assertMatchLike(0xF0, "vvvv1111", 0xFF)
    assertNotMatchLike("vvvv1111", 0xF0)
  }

  test("matchSeven") {
    assertMatchSeven(0x7F)
    assertNotMatchSeven(0xFF)
  }

  test("matchOneOf") {
    val m = matchOneOf(matchEquals(0xF), matchEquals(0x0))
    assert(Some(0xF) == m(0xF))
    assert(Some(0x0) == m(0x0))
    assert(None == m(0x9))
  }
}
