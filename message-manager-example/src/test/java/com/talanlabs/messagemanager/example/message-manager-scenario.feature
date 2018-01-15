Feature: Message Manager feature
  We create an engine, add TriggerEngine and Gate Factory addons.
  We then add an agent which listens to a folder

  Background:
    Given engine is created
    And dictionary is configured
    And agent is created and registered
    And engine is initialized

  Scenario: Injecting a file which is correct
    When a valid file is received
    Then the file should be accepted
    Then shutdown the engine

  Scenario: Injecting a file which is correct
    When a file with a warning error is received
    Then the file should be accepted
    Then shutdown the engine

  Scenario: Injecting a file which has a automatic recycling error
    When an invalid file with an automatic error is received
    Then the file should be in recycling automatically state
    Then shutdown the engine

  Scenario: Injecting a file which has a manual recycling error
    When an invalid file with a manual error is received
    Then the file should be in recycling manually state
    Then shutdown the engine

  Scenario: Injecting a file which has a reject definitely error
    When an invalid file with a reject definitely is received
    Then the file should be rejected
    Then shutdown the engine
