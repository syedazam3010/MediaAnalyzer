# MediaAnalyzer
## Setting up and running the application


* Use Intellij Idea as Code Editor
* Go to Edit Configrations on the top right corner of screen
* On top left you will see a '+' sign, click on it and select Application
* Select the main class as MediaAnalyzerApplication
* Open Terminal, and execute this maven goal -> mvn clean install. This will build our application
* Open Postman and Select POST method. Enter this url: http://localhost:8080/analyze
* Go to the body tab under URL pane, select raw as body data and paste this:
  {
  "url": "https://demo.castlabs.com/tmp/text0.mp4"
  }
* Click on Send Button on Postman

