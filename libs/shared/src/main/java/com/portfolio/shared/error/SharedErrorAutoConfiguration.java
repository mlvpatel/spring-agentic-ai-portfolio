package com.portfolio.shared.error;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Registers {@link GlobalExceptionHandler} for agent modules that depend on shared-lib.
 * Gateway (WebFlux) does not use this module.
 */
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class SharedErrorAutoConfiguration {
}
