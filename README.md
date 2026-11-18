# Enrollment Server - Wultra

Wultra implementation of the enrollment server. The enrollment server publishes the following services:

- PowerAuth Standard RESTful API
- Push Registration API
- Mobile Token Operations API
- Endpoints for Automated Testing

## Document Verification
The document verification process is currently supported for following providers:
- [ZenID](https://zenid.trask.cz/)
- Mock - useful for simple testing and local runs

### ZenID

#### Configuration - API key
The authorization of all API calls is secured by an API key value. It has to be sent as the `Authorization: api_key VALUE` header value. 
Check the bottom of the `Manual/Configuration` page for more details.

The API key value can be configured/get from the `Access` page configuration:
- Role to be granted: `ApiFull`
- Condition: `ApiKeyEqualsValue`
- Value: the value here is the value of the API key

#### Configuration - Validators
It is recommended to create a custom validation profile.  The sensitivity of selected validators can be tuned-up or disabled completely at the `Sensitivity` page.
The profile can be then set as the default or specified in the configuration properties.

#### SDK Initialization
When calling `document-verification/init-sdk` following implementation fields are used:
- Init token - send a token value `sdk-init-token` in the request body `attributes` map field 
- SDK response - receive the value under `zenid-sdk-init-response` from the response `attributes` map field

## Presence Check
The document verification process is currently supported for following providers:
- [iProov](https://www.iproov.com/)
- Mock - useful for simple testing and local runs

#### Configuration
There are a few needed configuration changes to bring a successful integration. All the following configuration tuning
has to be requested from the iProov's [support team](https://iproov.freshdesk.com/support/login) on a per-service basis:
- presence check image
  - an accepted person image from finished and successful presence check process
  - requires enabling of the `frame` response feature for [/api/v2/claim/verify/validate](https://secure.iproov.me/docs.html#operation/userVerifyValidate)
- failure reason
  - a more detailed description of the presence check failure
  - requires enabling of the `reason` response feature for [/api/v2/claim/verify/validate](https://secure.iproov.me/docs.html#operation/userVerifyValidate)


## Test coverage

Run `mvn test -Pcoverage` to collect JaCoCo coverage locally. Each module's HTML
report is at `target/site/jacoco/index.html`, alongside `jacoco.xml`. Coverage
also activates automatically in GitHub Actions; a regular local `mvn test` run
does not collect coverage.

The **Test with Maven** workflow publishes line and branch coverage for the
Enrollment Server and its production modules with tests in the run summary.
Totals are calculated from summed covered/total counters, not averaged module
percentages. Metrics with no applicable code are shown as `N/A`; missing or
invalid reports are reported explicitly, and the summary job fails. No minimum
coverage threshold is enforced.

Download the `maven-test-artifacts-<run-id>` artifact from the workflow run to
browse the HTML reports. The test-only mock adapter is not included.
