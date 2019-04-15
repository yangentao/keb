package dev.entao.keb.biz.model

import dev.entao.kava.base.Hex
import dev.entao.kava.sql.AND
import dev.entao.kava.sql.EQ
import dev.entao.kava.sql.Model
import dev.entao.kava.sql.ModelClass

/**
 * Created by entaoyang@163.com on 2018/4/2.
 */

//仅app用户
class TokenTable : Model() {

	@dev.entao.kava.sql.PrimaryKey
	var userId: String by model

	//os
	@dev.entao.kava.sql.PrimaryKey
	var type: String by model

	@dev.entao.kava.sql.Index
	var token: String by model

	//0, -1永不过期
	@dev.entao.kava.sql.Index
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