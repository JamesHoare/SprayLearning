organization  := "com.example"

version       := "0.1"

scalaVersion  := "2.10.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "io.spray"            %   "spray-can"     	% "1.2-M8",
  "io.spray"            %   "spray-caching"     	% "1.2-M8",
  "io.spray"            % "spray-servlet"       % "1.2-M8",
   "io.spray"            % "spray-util"       % "1.2-M8",
    "io.spray"            % "spray-http"       % "1.2-M8",
     "io.spray"            % "spray-io"       % "1.2-M8",
      "io.spray"            % "spray-httpx"       % "1.2-M8",
  "io.spray"            %   "spray-routing" 	% "1.2-M8",
  "io.spray"            %   "spray-testkit" 	% "1.2-M8" % "test",
  "com.typesafe.akka"   %%  "akka-actor"    	% "2.2.0-RC1",
  "com.typesafe.akka"   %%  "akka-testkit"  	% "2.2.0-RC1" % "test",
  "org.specs2"          %%  "specs2"        	% "1.14" % "test",
  "org.scalatest"       % "scalatest_2.10" % "2.0.M6-SNAP36" % "test",
  "org.mockito"         % "mockito-all" % "1.9.5" % "test",
  "org.json4s" 			%% 	"json4s-native" 	% "3.2.5",
  "org.mongodb" 		%% "casbah" 			% "2.6.2",
  "com.typesafe.akka" %% "akka-slf4j"           % "2.2.0-RC1",
  "org.elasticsearch"   % "elasticsearch"       % "0.90.3",
  "com.typesafe.slick" %% "slick" % "1.0.1",
  "mysql"              % "mysql-connector-java" % "5.1.25",
  "com.sksamuel.elastic4s" % "elastic4s_2.10" % "0.90.3.0",
  "org.scalastic" %% "scalastic" % "0.90.2",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.10.v20130312" % "container",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container" artifacts Artifact("javax.servlet", "jar", "jar"),
  "ch.qos.logback" 		% "logback-classic" 	% "1.0.3"
)

seq(webSettings: _*)












