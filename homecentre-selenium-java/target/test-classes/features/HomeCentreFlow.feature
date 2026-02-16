
@homecentre
Feature: Automate HomeCentre flow
  As a tester
  I want to automate the HomeCentre web flow end-to-end
  So that I can validate the UI and capture required artifacts

  Scenario: Complete flow from search to gift card
    Given I open Chrome and navigate to "https://www.homecentre.in/in/en"
    When I search for "book shelves" in the header search
    And I set the maximum price filter to 15000 and apply it
    And I expand the Type filter and select "Open"
    And I open the first product in results and capture related items
    And I save the "You may also like" items to an html file
    And I navigate to the Gifting section and click Shop Gift Card
    And I dismiss the updates popup with No thanks if it appears
    And I enter receiver name "mervillbenmen" and invalid email "hhh" then take a screenshot
    Then I quit the browser
