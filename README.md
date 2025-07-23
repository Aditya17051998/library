


use below command to deploy package on github

mvn deploy -DskipTests -X

Keep in that project where you are using library
@SpringBootApplication(scanBasePackages = {"org.paysecure.cardtoken", "org.paysecure.library"})