# Contributing to zchunk-java

Hey, you have opend up this file! Thanks a lot for thinking about contributions!

## Issues

* If it is a format specification problem, please head over to https://github.com/zchunk/zchunk.
* If you've spotted an implementation error, please be so kind as to deliver a simple test case (or a file which cannot be read).


## Pull Requests

Before creating pull requests, please tick these checkboxes in your mind:

 - [ ] Have you started a branch for exactly one issue?
 - [ ] If this is a PR for a issue, please name it issues/# (e.g. issues/3).
 - [ ] Before commiting and creating the PR, please execute:
   - [ ] ./mvnw clean install -DskipTests=true -Pcheckstyle,checker
   - [ ] ./mvnw -T4 clean install javadoc:jar sources:jar
   
If it doesn't compile, please fix the errors first. Thank you :)
   
