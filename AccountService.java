package cn.mymoments.service;

import java.sql.SQLException;
import java.util.List;

import javax.mail.MessagingException;
import cn.mymoments.dao.AccUpdateMode;
import cn.mymoments.dao.AccountDAO;
import cn.mymoments.dao.DAOFactory;
import cn.mymoments.domain.Account;
import cn.mymoments.exception.ActivateException;
import cn.mymoments.exception.FindBackPasswordException;
import cn.mymoments.exception.LoginException;
import cn.mymoments.exception.RegisterException;
import cn.mymoments.utils.MailUtils;

/**
 * @author 柳祺
 * 用户相关服务类
 */
public class AccountService {
	private AccountDAO dao = DAOFactory.getAccountDAOInstance();
	
	/**
	 * 注册服务方法
	 * @param account
	 * @throws RegisterException
	 */
	public void register(Account account, String basePath) throws RegisterException {
		try {
			Account acc = dao.findAccountbyEmailAddr(account.getEmailAddr());
			if (acc != null) {	//找到用户
				if (acc.getStatus() != 0) {	//用户已激活
					throw new RegisterException("{\"email-error-msg\":\"此邮箱已存在\", \"rst\":\"1\"}");
				}
				//用户重新注册
				account.setRegTime(new java.sql.Timestamp(System.currentTimeMillis()));
				dao.update(account, AccUpdateMode.REREGISTER);
			}
			else {	//用户注册
				dao.addAccount(account);
			}
			account = dao.findAccountbyEmailAddr(account.getEmailAddr());
			//发送激活邮件
			String emailMsg = "&nbsp;&nbsp;感谢您加入MyMemonts大家庭, 单击以下链接 <p>" +
					"<a href='http://localhost:8080" + basePath + "/ActivateAccountServlet" +
					"?activationCode=" + account.getActivationCode() + "&accountId=" +
					account.getAccountId() + "'>http://localhost:8080" + basePath +
					"/ActivateAccountServlet" + "?activationCode=" + account.getActivationCode() +
					"&accountId=" + account.getAccountId() + "</a></p>以激活您的账户。" +
					"同时为了您账户的安全，链接将在30分钟后失效！";
			MailUtils.sendMail(account.getEmailAddr(), "用户激活", emailMsg);
		} catch (SQLException | MessagingException e) {
			e.printStackTrace();
			throw new RegisterException("{\"reg-error-msg\":\"注册失败\", \"rst\":\"1\"}");
		}
	}
	
	/**
	 * 用户激活服务方法
	 * @param accountId
	 * @param activationCode
	 * @throws ActivateException 
	 */
	public void activate(Integer accountId, String activationCode) throws ActivateException {
		try {
			Account account = dao.findAccountbyId(accountId);
			if (account != null) {
				if (account.getActivationCode() != null && account.getActivationCode().equals(activationCode)) {
					long intval = System.currentTimeMillis() - account.getRegTime().getTime();
					if (intval <= 30 * 60 * 1000) {
						dao.activateAccount(accountId);
						return;
					}
					throw new ActivateException("此激活链接已失效,请重新注册");
				}
				throw new ActivateException("此为无效激活链接或链接已失效");
			}
			throw new ActivateException("此为无效激活链接");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ActivateException("激活失败，请重试");
		}
	}
	
	/**
	 * 登陆服务方法
	 * @param emailAddr
	 * @param password
	 * @return
	 * @throws LoginException
	 */
	public Account login(String emailAddr, String password) throws LoginException {
		try {
			Account account = dao.findAccountbyEmailAddr(emailAddr);
			if (account != null && account.getPassword().equals(password)) {
				if (account.getStatus() == 1) {
					return account;
				}
				else if (account.getStatus() == 0) {
					throw new LoginException("\"login-error-msg\":\"该用户未激活\",");
				}
				else if (account.getStatus() == 2) {
					throw new LoginException("\"login-error-msg\":\"该用户处于封禁期\",");
				}
			}
			throw new LoginException("\"login-error-msg\":\"邮箱不存在或密码错误\",");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new LoginException("\"login-error-msg\":\"登陆失败\",");
		}
	}
	
	public Account getAccountInfo(Integer accountId) {
		try {
			Account account =  dao.findAccountbyId(accountId);
			account.setActivationCode(null);
			account.setPassword(null);
			return account;
		} catch (SQLException e) {
			e.printStackTrace();
			return new Account(null, null, null, null, null,
					null, null, null, null);
		}
	}

	public Account checkAccoutByEmailAddr(String emailAddr) throws FindBackPasswordException{
		try {
			Account account = dao.findAccountbyEmailAddr(emailAddr);
			if (account != null) {	//找到用户
				switch (account.getStatus()) {
					case 0:	//未激活用户
						throw new FindBackPasswordException("\"email-error-msg\":\"此邮箱未激活，请重新注册\",");
					case 1:	//正常用户
						return account;
					case 2:	//封禁期用户
						throw new FindBackPasswordException("\"find-error-msg\":\"此账户处于封禁期，不支持找回密码\",");
					default: return null;
				}
			}
			else {	//未注册用户
				throw new FindBackPasswordException("\"email-error-msg\":\"此邮箱不存在\",");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new FindBackPasswordException("\"find-error-msg\":\"找回失败\",");
		}
	}

	public void sendResetPasswordLink(Account account, String basePath) throws FindBackPasswordException {
		try {
			dao.update(account, AccUpdateMode.FINDPASSWD);
			//发送重置邮件
			String emailMsg = "&nbsp;&nbsp;感谢您使用MyMemonts, 单击以下链接 <p>" +
					"<a href='http://localhost:8080" + basePath + "/ResetLinkServlet" +
					"?resetCode=" + account.getActivationCode() + "&accountId=" +
					account.getAccountId() + "'>http://localhost:8080" + basePath +
					"/ResetLinkServlet" + "?resetCode=" + account.getActivationCode() +
					"&accountId=" + account.getAccountId() + "</a></p> 以重置您账户的密码。" +
					"同时为了您账户的安全，链接将在30分钟后失效！若非本人操作，请留意账户信息是否泄露。并请不要将该链接随意转发他人，以免上当受骗！";
			MailUtils.sendMail(account.getEmailAddr(), "密码重置", emailMsg);
		} catch (MessagingException | SQLException e) {
				e.printStackTrace();
				throw new FindBackPasswordException("\"find-error-msg\":\"找回失败\",");
		}
	}
	
	public void resetPassword(String emailReq, String password) throws FindBackPasswordException {
		Account account = new Account();
		account.setEmailAddr(emailReq);
		account.setPassword(password);
		try {
			dao.update(account, AccUpdateMode.RESETPASSWD);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new FindBackPasswordException("\\\"reset-error-msg\\\":\\\"请求失败\\\",");
		}
		
	}


	//新增内容
	public boolean modifyAccountInfo(Account account){
        try {
            Integer num = dao.update(account, AccUpdateMode.MODIFY);
            if(num == 0){
                return false;
            }
            else{
                return  true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return  false;
        }
    }
    public boolean modifyPasswordByAccountId(Integer accountId, String password){
	    try {
            Account account = dao.findAccountbyId(accountId);
            account.setPassword(password);
            Integer num = dao.update(account, AccUpdateMode.PASSWD);
            if(num == 0){
                return  false;
            }
            else{
                return true;
            }
        }catch (SQLException e){
	        e.printStackTrace();
	        return false;
        }
    }
	public void deleteAccount(Integer accountId) {
		try {
			dao.delAccountById(accountId);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void bannedState(Integer accountId, Integer statusId) {
		try {
			dao.changeState(accountId, statusId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Account> findAccountByEmail(String email) {
		try {
			if(dao.findAccountbyEmail(email).isEmpty()) {
				return dao.findAllAccounts();
			}
			else {
				return dao.findAccountbyEmail(email);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
