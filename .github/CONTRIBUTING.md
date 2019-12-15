# Pull Requests

Pull requests are encouraged! To increase the likelihood of your PR being pulled, here are some guidelines to follow:

- Each PR must address only a single feature/improvement
  - This makes for an easier-to-read diff and focused feedback.
  - PRs that aim to solve multiple issues causes scattered feedback and a hard-to-follow diff

- Maintain consistency where possible
  - Newline braces
  - The majority of 16.x uses tabs
  - Version 20+ will uses spaces
  - This guideline will be updated when version 20 becomes generally available

- Do not change version in pom.xml.

- Feel free to contribute to the dev/v20 branch (may be advised to discuss in #63)

## Tips
- Avoid nesting - check for the inverted value and return early (not easy to do though in legacy versions where many methods are "uber methods" containing a lot of control flow).
- Use `Entity#getType` instead of `instanceof` where appropriate (`getType` is faster).

# Issues

- Try to search for your bug/suggestion first. Someone may have already created one.
- If not, please fill out the template for the respective issue you are creating.
