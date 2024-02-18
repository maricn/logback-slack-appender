# Logback Slack appender

## STATUS UPDATE 2024-02-18
Check out https://github.com/cyfrania/logback-slack-appender for up-to-date version (https://github.com/maricn/logback-slack-appender/issues/38).

### UNMANTAINED
I don't have time to take care and maintain this project anymore as I don't use logback on any of my projects. If anyone wants to have their fork linked from here, or to take over this repo, drop an issue. Take a look at a newer fork https://github.com/cyfrania/logback-slack-appender. Thanks for your support so far!

This is a simple [Logback](http://logback.qos.ch/) appender which pushes logs to [Slack](https://slack.com/) channel.

## How to setup

Add dependency to `com.github.maricn:logback-slack-appender` in your `pom.xml`.

Add SlackAppender configuration to `logback.xml` file

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
	...
	<appender name="SLACK" class="com.github.maricn.logback.SlackAppender">
		<!-- Slack API token -->
		<token>1111111111-1111111-11111111-111111111</token>
		<!-- Slack incoming webhook uri. Uncomment the lines below to use incoming webhook uri instead of API token. -->
		<!--
		<webhookUri>https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX</webhookUri>
		-->
		<!-- Channel that you want to post - default is #general -->
		<channel>#api-test</channel>
		<!-- Formatting (you can use Slack formatting - URL links, code formatting, etc.) -->
		<layout class="ch.qos.logback.classic.PatternLayout">
			<pattern>%-4relative [%thread] %-5level %class - %msg%n</pattern>
		</layout>
		<!-- Username of the messages sender -->
		<username>${HOSTNAME}</username>
		<!-- Emoji to be used for messages -->
		<iconEmoji>:stuck_out_tongue_winking_eye:</iconEmoji>
		<!-- If color coding of log levels should be used -->
		<colorCoding>true</colorCoding>
	</appender>
	<!-- Currently recommended way of using Slack appender -->
	<appender name="ASYNC_SLACK" class="ch.qos.logback.classic.AsyncAppender">
		<appender-ref ref="SLACK" />
		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>ERROR</level>
		</filter>
	</appender>
	<root>
		<level value="ALL" />
		<appender-ref ref="ASYNC_SLACK" />
	</root>
</configuration>
```

Example of `SlackAppender` configuration to `logback.groovy` file (filtering by marker):

```java
import com.github.maricn.logback.SlackAppender
import ch.qos.logback.core.filter.EvaluatorFilter
import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.boolex.OnMarkerEvaluator

appender('SLACK', SlackAppender) {
  channel = "#api-test"
  username = "${HOSTNAME}"
  colorCoding = "true"
  webhookUri = "https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX"
  layout(PatternLayout) {
    pattern = "%-4relative [%thread] %-5level %class - %msg%n"
  }
  filter(EvaluatorFilter) {
    evaluator(OnMarkerEvaluator) {
      marker = "SLACK_MARKER"
    }
    onMismatch = DENY
    onMatch = NEUTRAL
  }
}

appender('ASYNC_SLACK', AsyncAppender) {
  appenderRef("SLACK")
}
```
