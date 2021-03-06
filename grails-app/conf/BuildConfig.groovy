mogobiz.version="${appVersion}"
grails.project.work.dir = "target"

grails.project.fork = [
        // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
        //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

        // configure settings for the test-app JVM, uses the daemon by default
        test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
        // configure settings for the run-app JVM
        run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
        // configure settings for the run-war JVM
        war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
        // configure settings for the Console UI JVM
        console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // compile "org.springframework:spring-orm:$springVersion"
        compile ('org.codehaus.groovy.modules.http-builder:http-builder:0.5.2') { excludes "groovy" }
        compile 'com.fasterxml.jackson.core:jackson-core:2.7.0'
        compile 'com.fasterxml.jackson.core:jackson-databind:2.7.0'
        compile group:"org.twitter4j", name:"twitter4j-async", version:"2.2.5"
        compile group:"org.twitter4j", name:"twitter4j-core", version:"2.2.5"
        compile group:"org.twitter4j", name:"twitter4j-media-support", version:"2.2.5"
        compile group:"org.twitter4j", name:"twitter4j-stream", version:"2.2.5"
//        runtime 'mysql:mysql-connector-java:5.1.30'
        runtime "postgresql:postgresql:9.1-901.jdbc4"
        //runtime "com.oracle:ojdbc6:11.2.0.1.0"

        compile 'org.scala-lang:scala-library:2.11.2'

        runtime 'com.typesafe.akka:akka-actor_2.11:2.3.12'
        runtime 'com.typesafe.akka:akka-stream-experimental_2.11:1.0-M3'

        compile (group:"io.reactivex", name:"rxjava-reactive-streams", version: "1.0.1") {excludes ([ group: 'io.reactivex', name: 'rxjava'])}
        compile ('io.reactivex:rxgroovy:1.0.3') {excludes "groovy-all"}
        compile ('io.reactivex:rxjava:1.0.15')

        //oltu
        compile 'org.apache.oltu.oauth2:org.apache.oltu.oauth2.common:0.31'
        compile 'org.apache.oltu.oauth2:org.apache.oltu.oauth2.authzserver:0.31'
        compile 'org.apache.oltu.oauth2:org.apache.oltu.oauth2.resourceserver:0.31'
        runtime 'org.codehaus.jettison:jettison:1.2'

        compile 'com.restfb:restfb:1.6.7'
        compile 'com.google.zxing:core:1.7'
        compile 'org.apache.poi:poi-ooxml:3.10.1'

        compile 'org.jsoup:jsoup:1.8.1'

        compile (group:"com.mogobiz", name:"mogobiz-tools", version:"${mogobiz.version}")  {excludes "groovy-all"}

        compile (group:"com.mogobiz.rivers", name:"mogobiz-common", version:"${mogobiz.version}")  {excludes "groovy-all"}
        compile (group:"com.mogobiz.rivers", name:"mogobiz-elasticsearch", version:"${mogobiz.version}")  {excludes "groovy-all"}
        compile (group:"com.mogobiz.rivers", name:"mogobiz-mirakl", version:"${mogobiz.version}")  {excludes "groovy-all"}
    }

    plugins {
        build(":release:3.0.1",
                ":rest-client-builder:1.0.3") {
            export = false
        }

// plugins needed at runtime but not for compilation
        runtime ':hibernate:3.6.10.15' // ':hibernate4:4.3.5.3' for Hibernate 4
        runtime ':database-migration:1.4.0'
        compile ":facebook-graph:0.14"
        compile ':platform-core:1.0.0'
        compile ":google-data:0.1.3"
        runtime ":resources:1.2.8"
        compile (":email-confirmation:2.0.8") {
            excludes 'quartz'
        }
        compile (":shiro:1.2.1") {
            excludes 'shiro-quartz'
        }
        compile ":mail:1.0.5"
        compile ":quartz:1.0.1"
        compile ':recaptcha:0.6.8'
        compile ":cache-headers:1.1.6"
        compile ":cached-resources:1.0"
        compile ":rest:0.8"
        compile ":joda-time:1.4"
        compile ":executor:0.3"
    }
}
