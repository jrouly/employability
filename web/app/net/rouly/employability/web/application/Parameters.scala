package net.rouly.employability.web.application

import net.rouly.common.config.Configuration

class Parameters(config: Configuration) {

  /**
    * Minimum relevance of a topic for overlap.
    */
  val rho: Double = config.get("rho", "0.02").toDouble

  /**
    * Intersection parameter for overlap.
    */
  val theta: Double = config.get("theta", "0.055").toDouble

}
