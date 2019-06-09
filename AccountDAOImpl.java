package cn.mymoments.daoimpl;

import java.sql.SQLException;
import java.util.List;

import cn.mymoments.dao.AccUpdateMode;
import cn.mymoments.dao.AccountDAO;
import cn.mymoments.dao.BaseDAO;
import cn.mymoments.domain.Account;

/**
 * @author 柳祺
 * 用户数据库连接接口实现类
 */
public class AccountDAOImpl extends BaseDAO<Account> implements AccountDAO {

	@Override
	public void addAccount(Account account) throws SQLException {
		String sql = "INSERT INTO accounts(email_addr,nickname,password,portrait_url,activation_code)"
					+ " VALUES(?,?,?,?,?);";
		this.update(sql, account.getEmailAddr(), account.getNickname(), account.getPassword(),
					account.getPortraitURL(), account.getActivationCode());
	}

	@Override
	public void activateAccount(Integer accountId) throws SQLException {
		String sql = "UPDATE accounts SET status = ? WHERE account_id = ?;";
		this.update(sql, 1, accountId);
	}

	@Override
	public int update(Account account, AccUpdateMode mode) throws SQLException {
		int row = 0;
		String sql;
		switch (mode) {
		case PASSWD:
			sql = "UPDATE accounts SET password=? WHERE account_id=?";
			row = this.update(sql, account.getPassword(), account.getAccountId());
			break;
		case REREGISTER:
			sql =  "UPDATE accounts SET nickname = ?, password = ?, activation_code = ?, reg_time = ?"
						+" WHERE email_addr = ?";
			row = this.update(sql, account.getNickname(), account.getPassword(),
						account.getActivationCode(), account.getRegTime(), account.getEmailAddr());
			break;
		case MODIFY:
            sql = "UPDATE accounts SET nickname=?, style_no=?, signature=? WHERE account_id=?";
            row = this.update(sql, account.getNickname(),account.getStyleNo(),account.getSignature(), account.getAccountId());
			break;
		case FINDPASSWD:
			sql =  "UPDATE accounts SET activation_code = ? WHERE email_addr = ?";
			row = this.update(sql, account.getActivationCode(), account.getEmailAddr());
			break;
		case RESETPASSWD:
			sql =  "UPDATE accounts SET password = ? WHERE email_addr = ?";
			row = this.update(sql, account.getPassword(), account.getEmailAddr());
			break;
		default:
			break;
		}
		return row;
	}

	@Override
	public Account findAccountbyId(int accountId) throws SQLException {
		String sql = "SELECT * FROM accounts WHERE account_id = ?;";
		return this.get(sql, accountId);
	}

	@Override
	public Account findAccountbyEmailAddr(String emailAddr) throws SQLException {
		String sql = "SELECT * FROM accounts WHERE email_addr = ?;";
		return this.get(sql, emailAddr);
	}

	@Override
	public List<Account> findAllAccounts() throws SQLException {
		String sql = "SELECT * FROM accounts;";
		return this.getForList(sql);
	}

	@Override
	public boolean delAccountById(int accountId) throws SQLException {
		String sql = "DELETE FROM accounts WHERE account_id = ?;";
		try {
			int flag = this.update(sql, accountId);
			if(flag != 0)
			{
				return true;
			}
			else {
				return false;
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public void changeState(int accountId, int statusId) throws SQLException {
		String sql = "UPDATE accounts SET status = ? WHERE account_id = ?;";
		this.update(sql, statusId, accountId);
	}

	@Override
	public List<Account> findAccountbyEmail(String email) throws SQLException {
		String sql = "SELECT * FROM accounts WHERE email_addr = ?;";
		return this.getForList(sql, email);
	}
}
