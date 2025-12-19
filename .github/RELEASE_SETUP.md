# Maven Central Release Setup

This document describes how to configure GitHub Actions to automatically publish releases to Maven Central.

## Prerequisites

1. A Maven Central account (via [central.sonatype.org](https://central.sonatype.org))
2. A GPG key pair for signing artifacts

## Required GitHub Secrets

Configure the following secrets in your GitHub repository settings (Settings → Secrets and variables → Actions):

### 1. `MAVEN_CENTRAL_USERNAME`
Your Maven Central username/token username.

For Central Portal (recommended):
- Go to https://central.sonatype.com/account
- Generate a new token
- Use the token username

### 2. `MAVEN_CENTRAL_TOKEN`
Your Maven Central password/token password.

For Central Portal:
- Use the token password from the same token generation step above

### 3. `GPG_PRIVATE_KEY`
Your GPG private key used for signing artifacts.

To export your GPG private key:
```bash
# List your keys
gpg --list-secret-keys --keyid-format=long

# Export the key (replace KEY_ID with your key ID)
gpg --armor --export-secret-keys KEY_ID
```

Copy the entire output including the `-----BEGIN PGP PRIVATE KEY BLOCK-----` and `-----END PGP PRIVATE KEY BLOCK-----` lines.

### 4. `GPG_PASSPHRASE`
The passphrase for your GPG private key.

## Generating a GPG Key (if you don't have one)

```bash
# Generate a new key
gpg --gen-key

# Follow the prompts:
# - Use your real name
# - Use your email
# - Choose a strong passphrase

# List your keys to verify
gpg --list-secret-keys --keyid-format=long

# Upload your public key to a key server
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
gpg --keyserver keys.openpgp.org --send-keys KEY_ID
```

## Triggering a Release

There are two ways to trigger a release:

### 1. Tag-based Release (Recommended)
```bash
# Create and push a version tag
git tag v3.0.0
git push origin v3.0.0
```

The workflow will automatically:
- Build the project
- Sign the artifacts
- Deploy to Maven Central
- Create a GitHub release

### 2. Manual Release
Go to Actions → Release to Maven Central → Run workflow and enter the version number.

## Verifying the Release

1. Check the GitHub Actions workflow run for any errors
2. Verify the artifacts on [Maven Central](https://central.sonatype.com/)
3. Check the [Maven Central Repository](https://repo1.maven.org/maven2/com/vgerbot/propify/) (may take a few hours to sync)

## Troubleshooting

### GPG Signing Issues
- Ensure the GPG key is properly formatted with newlines
- Verify the passphrase is correct
- Check that the key hasn't expired: `gpg --list-keys`

### Maven Central Authentication Issues
- Verify your credentials are correct
- Ensure your token hasn't expired
- Check that you have the necessary permissions for the groupId `com.vgerbot`

### Deployment Failures
- Review the GitHub Actions logs
- Ensure all required metadata is present in the POM (name, description, url, licenses, developers, scm)
- Verify that source and javadoc JARs are being generated

## Maven Central Requirements

To successfully publish to Maven Central, your artifacts must include:
- ✅ Project metadata (name, description, URL) - Already configured
- ✅ License information - Already configured
- ✅ Developer information - Already configured
- ✅ SCM information - Already configured
- ✅ Source JAR - Configured via maven-source-plugin
- ✅ Javadoc JAR - Configured via maven-javadoc-plugin
- ✅ GPG signatures - Configured via maven-gpg-plugin in release profile

All requirements are already satisfied in your `propify/pom.xml`.
