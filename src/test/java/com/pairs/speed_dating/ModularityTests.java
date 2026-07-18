package com.pairs.speed_dating;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {
  static final ApplicationModules modules = ApplicationModules.of(SpeedDatingApplication.class);

  @Test
  void checkIfModulesStructureIsConsistent() {
    modules.verify();
  }
}
