
# 🔨 Rebuild & Install Library Locally

mvn clean install -Dgpg.skip


# 🚀 Deploying to Shared Repo (Optional)

If you want others (team/CI/CD) to use it:

mvn clean deploy -DskipTests -P gpg