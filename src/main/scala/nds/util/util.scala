package nds

package object util {
  implicit class fromIntToHz(v: Int) {
    def Hz: Int = v
    def kHz: Int = v * 1000
    def MHz: Int = v * 1000 * 1000
    def GHz: Int = v * 1000 * 1000 * 1000
  }
  implicit  class fromDoubleToHz(v: Double) {
    def Hz: Int = v.toInt
    def kHz: Int = (v * 1000).toInt
    def MHz: Int = (v * 1000 * 1000).toInt
    def GHz: Int = (v * 1000 * 1000 * 1000).toInt
  }
}