package moe.pizza.auth.adapters

import com.ning.http.util.Base64
import moe.pizza.auth.interfaces.UserDatabase
import moe.pizza.auth.ldap.client.LdapClient
import moe.pizza.auth.models.Pilot
import moe.pizza.auth.ldap.client.Utils
import org.apache.directory.api.ldap.model.entry.DefaultEntry
import org.apache.directory.api.ldap.model.message.{ResultCodeEnum, AddRequestImpl, AddRequest}
import org.apache.directory.api.ldap.model.schema.SchemaManager


/**
  * Created by andi on 03/03/16.
  */
class LdapUserDatabase(client: LdapClient, schema: SchemaManager) extends UserDatabase {

  implicit class ConvertablePilot(p: Pilot) {
    def toAddRequest(s: SchemaManager, password: String) = {
      val e = new DefaultEntry(s,
        s"uid=${p.uid},ou=pizza",
        "ObjectClass: top",
        "ObjectClass: pilot",
        "ObjectClass: account",
        "ObjectClass: simpleSecurityObject",
        s"corporation: ${p.corporation}",
        s"alliance: ${p.alliance}",
        s"accountStatus: ${p.accountStatus.toString}",
        s"email: ${p.email}",
        s"uid: ${p.uid}",
        s"metadata: ${p.metadata.toString}",
        s"characterName: ${p.characterName}",
        s"userpassword: {SSHA} ${Base64.encode(Utils.hashPassword(password))}"
      )
      //e.add("crestKey", p.crestTokens:_*)
      val add = new AddRequestImpl()
      add.setEntry(e)
      add
    }
  }

  override def addUser(p: Pilot, password: String): Boolean = {
    var res = false
    client.withConnection{ c =>
      val r = c.add(p.toAddRequest(schema, password))
      if (r.getLdapResult.getResultCode == ResultCodeEnum.SUCCESS)
        res = true
    }
    res
  }

  override def deleteUser(p: Pilot): Boolean = true

  override def authenticateUser(uid: String, password: String): Option[Pilot] = None

  override def getUser(uid: String): Option[Pilot] = None

  override def getUsers(filter: String): List[Pilot] = List()

  override def updateUser(p: Pilot): Boolean = true

  override def setPassword(p: Pilot, password: String): Boolean = true

  override def getAllUsers(): Seq[Pilot] = Seq()
}