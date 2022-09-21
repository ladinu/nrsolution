# Solution

## Usage

Download the zip files, extract them and run the shell script in `bin/` folder. For the main app it
is `app-0.0.1/bin/main` and `tests-0.0.1/bin/main` for the tests.

Please note that this requires JVM 11+. Make sure that you have `JAVA_HOME` environment variable properly set.
To check, run the following

```bash
$ java -version
openjdk version "11.0.11" 2021-04-20 LTS
OpenJDK Runtime Environment Zulu11.48+21-CA (build 11.0.11+9-LTS)
OpenJDK 64-Bit Server VM Zulu11.48+21-CA (build 11.0.11+9-LTS, mixed mode)
````

## Limitations

* Doesn't handle all unicode chars
* Doesn't handle errors gracefully

## Future Improvements

* Add better error messages (like when files are not found)
* Add property based tests and a real test framework 
* Add more test cases