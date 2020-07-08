Feature: csrf and log-out endpoint

Background:
  * url baseUrl
  * def util = Java.type('karate.KarateTests')


Scenario: user page
	Given path '/perfil'
	* call read('login1.feature')
    Given param idUsuario = 1
    When method GET
    Then status 200
    * string response = response
    * def userName = util.selectHtml(response, "span")
    And match response contains 'Abundio Ejemplez'
