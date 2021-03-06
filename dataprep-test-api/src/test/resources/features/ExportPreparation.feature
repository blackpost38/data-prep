Feature: Export Preparation

  Scenario: Verify transformation result
    Given I upload the dataset "/data/simpleCSV.csv" with name "simpleCSV"
    And I create a preparation with name "myFirstPreparation", based on "simpleCSV" dataset
    When I add a step with parameters :
      | actionName      | uppercase          |
      | columnName      | lastname           |
      | columnId        | 0001               |
      | preparationName | myFirstPreparation |
    And I export the preparation "myFirstPreparation" on the dataset "simpleCSV" and export the result in "myResult.csv" temporary file.
    Then I check that "myResult.csv" temporary file equals "/data/simpleCSV_processed.csv" file
