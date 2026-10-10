Screen-AI Gradle bootstrap files

Copy these into the ROOT of your GitHub repository:
- gradlew
- gradlew.bat
- gradle/wrapper/gradle-wrapper.properties

This lightweight bootstrap downloads Gradle 8.9 at runtime and then executes it.
It is not the standard Gradle-generated wrapper (it does not include gradle-wrapper.jar).
If your Android Gradle Plugin requires a different Gradle version, update VERSION in
gradlew and gradlew.bat and distributionUrl in gradle-wrapper.properties to match.

After uploading, ensure gradlew is executable in Git:
  chmod +x gradlew
On GitHub Actions, you can also use:
  chmod +x gradlew
