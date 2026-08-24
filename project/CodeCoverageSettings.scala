import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {
  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    ".*.Module",
    "config.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "prod.*",
    ".*Routes.*",
    "testOnly.*",
    "testOnlyDoNotUseInAppConf.*",
    ".*handlers.*",
    ".*components.*",
    ".*viewmodels.govuk.*",
    "controllers.LanguageSwitchController",
    "models.UserAnswers",
    "pages.*",
    "queries.*",
    ".*Algebra",
    "repositories.*",
    "viewmodels.*",
    "views.html.*",
    "views.ViewUtils",
    "views.html.ErrorTemplate",
    "models.Enumerable",
    "models.Mode",
    "forms.mappings.Formatters",
    "views.html.IndexView",
    "views.html.CheckYourAnswersView"
  )

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 50,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
