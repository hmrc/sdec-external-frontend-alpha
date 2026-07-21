import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.7.0"
  private val hmrcMongoVersion = "2.12.0"
  private val hmrcPlayFrontend = "12.32.1"
  private val scalaChkVersion  = "3.2.17.0"
  private val awsSdkVersion    = "2.47.5"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30" % hmrcPlayFrontend,
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "software.amazon.awssdk" % "s3"                         % awsSdkVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus" %% "scalacheck-1-17"         % scalaChkVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
