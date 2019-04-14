package yet.servlet

import dev.entao.sql.Index
import dev.entao.sql.PrimaryKey
import dev.entao.sql.AND
import dev.entao.sql.EQ
import dev.entao.sql.Model
import dev.entao.sql.ModelClass
import dev.entao.kbase.Hex

/**
 * Created by entaoyang@163.com on 2018/4/2.
 */

//仅app用户
class TokenTable : Model() {

	@PrimaryKey
	var userId: String by model

	//os
	@PrimaryKey
	var type: String by model

	@Index
	var token: String by model

	//0, -1永不过期
	@Index
	var expire: Long by model

	val isExpired: Boolean
		get() {
			return if (expire != 0L && expire != -1L) {
				expire > System.currentTimeMillis()
			} else {
				false
			}
		}

	companion object : ModelClass<TokenTable>() {
		fun remove(user: String, os: String) {
			TokenTable.delete(TokenTable::userId EQ user AND (TokenTable::type EQ os))
		}

		fun refresh(user: String, os: String, expire: Long = 0): String {
			val w = (TokenTable::userId EQ user) AND (TokenTable::type EQ os)
			val tt = TokenTable.findOne(w)
			if (tt == null) {
				val s = "$user|$os|$expire"
				val t = TokenTable()
				t.userId = user
				t.type = os
				t.expire = expire
				t.token = Hex.encode(s.toByteArray())
				t.insert()
				return t.token
			} else {
				tt.expire = expire
				tt.updateByKey(tt::expire)
				return tt.token
			}
		}
	}
}