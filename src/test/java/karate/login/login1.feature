Feature: csrf and sign-in end point

Background:
* url baseUrl
* def util = Java.type('karate.KarateTests')

Given path 'login'
When method get
Then status 200
* string response = response    
* def csrf = util.selectAttribute(response, "input[name=_csrf]", "value");
* print csrf



Scenario: html url encoded form submit - post
    Given path 'login'
    And form field username = 'b'
    And form field password = 'aa'
    And form field _csrf = csrf
    When method post
    Then status 200
    * string response = response    
    * def h2s = util.selectHtml(response, "h2");
    * print h2s
    And match h2s contains 'Panel de control'
