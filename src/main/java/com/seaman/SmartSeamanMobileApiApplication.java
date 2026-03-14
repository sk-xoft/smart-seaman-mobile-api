package com.seaman;

import com.seaman.constant.AppSys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.TimeZone;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@OpenAPIDefinition(info = @Info(title = AppSys.APPLICATION_NAME, version = AppSys.APPLICATION_VERSION, description = AppSys.APPLICATION_DESC))
// @SecurityScheme(name = AppSys.APPLICATION_NAME, scheme = "basic", type =
// SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
@EnableCaching
public class SmartSeamanMobileApiApplication {

	private final Logger logger = LoggerFactory.getLogger(SmartSeamanMobileApiApplication.class);

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Value("${smart.seaman.datasource.url}")
	private String dbUrl;

	@Value("${smart.seaman.datasource.username}")
	private String dbUsername;

	@Value("${smart.seaman.datasource.password}")
	private String dbPassword;

	@Value("${object.store.endpoint}")
	private String objectStoreEndpoint;

	@Value("${object.store.bucket}")
	private String objectStoreBucket;

	@Value("${object.store.key}")
	private String objectStoreKey;

	@Value("${fcm.firebase.credential.file}")
	private String fcmCredentialFile;

	@Value("${spring.mail.host}")
	private String mailHost;

	@Value("${spring.mail.username}")
	private String mailUsername;

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${encrypt.cert.key}")
	private String jwdKey;

	public static void main(String[] args) {

		// this test logs.
		// logger.trace("A TRACE Message");
		// logger.debug("A DEBUG Message");
		// logger.info("An INFO Message");
		// logger.warn("A WARN Message");
		// logger.error("An ERROR Message");

		SpringApplication.run(SmartSeamanMobileApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			StringBuilder sb = new StringBuilder();
			sb
					.append(System.lineSeparator())
					.append("|------------Application Details ------------")
					.append(System.lineSeparator())
					.append("|--- Application name : ").append(AppSys.APPLICATION_NAME)
					.append(System.lineSeparator())
					.append("|--- Environment : ").append(activeProfile)
					.append(System.lineSeparator())
					.append("|--- Default Timezone : ").append(TimeZone.getDefault().getID())
					.append(" at Date : ").append(new Date())
					.append(System.lineSeparator())
					.append("|--- Application Charset : ").append(Charset.defaultCharset().displayName())
					.append(System.lineSeparator())
					.append("|------------Config Details ------------")
					.append(System.lineSeparator())
					.append("|--- DB URL             : ").append(dbUrl)
					.append(System.lineSeparator())
					.append("|--- DB Username        : ").append(dbUsername)
					.append(System.lineSeparator())
					.append("|--- DB Password        : ").append(dbPassword.isEmpty() ? "(empty)" : "****")
					.append(System.lineSeparator())
					.append("|--- Object Store URL   : ").append(objectStoreEndpoint)
					.append(System.lineSeparator())
					.append("|--- Object Store Bucket: ").append(objectStoreBucket)
					.append(System.lineSeparator())
					.append("|--- Object Store Key   : ")
					.append(objectStoreKey.isEmpty() ? "(empty)"
							: objectStoreKey.substring(0, Math.min(4, objectStoreKey.length())) + "****")
					.append(System.lineSeparator())
					.append("|--- FCM Credential File: ").append(fcmCredentialFile)
					.append(System.lineSeparator())
					.append("|--- Mail Host          : ").append(mailHost)
					.append(System.lineSeparator())
					.append("|--- Mail Username      : ").append(mailUsername)
					.append(System.lineSeparator())
					.append("|--- JWT Secret         : ")
					.append(jwtSecret.isEmpty() ? "(empty)"
							: jwtSecret.substring(0, Math.min(4, jwtSecret.length())) + "****")
					.append(System.lineSeparator())
					.append("|--- JWT Key         : ")
					.append(jwdKey.isEmpty() ? "(empty)"
							: jwdKey.substring(0, Math.min(4, jwdKey.length())) + "****")
					.append(System.lineSeparator())
					.append("|---------------------------------");

			logger.info("{}", sb);
		};
	}
}
