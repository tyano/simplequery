import com.amazonaws.services.simpledb.*
import com.amazonaws.auth.*
import java.io.*

def getSecurityCredentialPath() {
	"/Users/t_yano/aws.credential.properties"
}

before "simpledbクライアントの作成", {
    given "create a client.", {
        credential = new PropertiesCredentials(new File(getSecurityCredentialPath()));
        sdb = new AmazonSimpleDBClient(credential)
    }
}

scenario "the number of domains should be 1 and the name should be 'sample'.", {
    when "サンプルDBからドメインを取得すると,", {
        result = sdb.listDomains()
        domains = result.domainNames
    }

    then "ドメイン数は1で名前は'sample'であること。", {
        domains.size().shouldBe 1
        domains[0].shouldBe "sample"
    }
}