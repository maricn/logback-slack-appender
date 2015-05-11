This is a simple [Logback](http://logback.qos.ch/) appender which pushes logs to Slack channel.

# How to setup

Add dependency to rs.maric.nikola:logback-hipchat-appender:1.0.0 in your pom.xml.

Add SlackAppender configuration to logback.xml file

```
	<?xml version="1.0" encoding="UTF-8" ?>
	<configuration>
		...
		<appender name="SLACK" class="rs.maric.nikola.logback.SlackAppender">
			<!-- Slack API token -->
			<token>1111111111-1111111-11111111-111111111</token>
			<!-- Channel that you want to post - default is #general -->
			<channel>#api-test</channel>
			<!-- Formatting -->
			<layout class="ch.qos.logback.classic.PatternLayout">
				<pattern>%-4relative [%thread] %-5level %class - %msg%n</pattern>
			</layout>
		</appender>

		<root>
			<level value="ALL" />
			<appender-ref ref="SLACK" />
		</root>

	</configuration>
```
