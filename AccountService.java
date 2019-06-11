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
 * @author ����
 * �û���ط�����
 */
public class AccountService {
	private AccountDAO dao = DAOFactory.getAccountDAOInstance();
	
	/**
	 * ע����񷽷�
	 * @param account
	 * @throws RegisterException
	 */
	public void register(Account account, String basePath) throws RegisterException {
		try {
			Account acc = dao.findAccountbyEmailAddr(account.getEmailAddr());
			if (acc != null) {	//�ҵ��û�
				if (acc.getStatus() != 0) {	//�û��Ѽ���
					throw new RegisterException("{\"email-error-msg\":\"�������Ѵ���\", \"rst\":\"1\"}");
				}
				//�û�����ע��
				account.setRegTime(new java.sql.Timestamp(System.currentTimeMillis()));
				dao.update(account, AccUpdateMode.REREGISTER);
			}
			else {	//�û�ע��
				dao.addAccount(account);
			}
			account = dao.findAccountbyEmailAddr(account.getEmailAddr());
			//���ͼ����ʼ�
			String emailMsg = "&nbsp;&nbsp;��л������MyMemonts���ͥ, ������������ <p>" +
					"<a href='http://localhost:8080" + basePath + "/ActivateAccountServlet" +
					"?activationCode=" + account.getActivationCode() + "&accountId=" +
					account.getAccountId() + "'>http://localhost:8080" + basePath +
					"/ActivateAccountServlet" + "?activationCode=" + account.getActivationCode() +
					"&accountId=" + account.getAccountId() + "</a></p>�Լ��������˻���" +
					"ͬʱΪ�����˻��İ�ȫ�����ӽ���30���Ӻ�ʧЧ��";
			MailUtils.sendMail(account.getEmailAddr(), "�û�����", emailMsg);
		} catch (SQLException | MessagingException e) {
			e.printStackTrace();
			throw new RegisterException("{\"reg-error-msg\":\"ע��ʧ��\", \"rst\":\"1\"}");
		}
	}
	
	/**
	 * �û�������񷽷�
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
					throw new ActivateException("�˼���������ʧЧ,������ע��");
				}
				throw new ActivateException("��Ϊ��Ч�������ӻ�������ʧЧ");
			}
			throw new ActivateException("��Ϊ��Ч��������");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ActivateException("����ʧ�ܣ�������");
		}
	}
	
	/**
	 * ��½���񷽷�
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
					throw new LoginException("\"login-error-msg\":\"���û�δ����\",");
				}
				else if (account.getStatus() == 2) {
					throw new LoginException("\"login-error-msg\":\"���û����ڷ����\",");
				}
			}
			throw new LoginException("\"login-error-msg\":\"���䲻���ڻ��������\",");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new LoginException("\"login-error-msg\":\"��½ʧ��\",");
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
			if (account != null) {	//�ҵ��û�
				switch (account.getStatus()) {
					case 0:	//δ�����û�
						throw new FindBackPasswordException("\"email-error-msg\":\"������δ���������ע��\",");
					case 1:	//�����û�
						return account;
					case 2:	//������û�
						throw new FindBackPasswordException("\"find-error-msg\":\"���˻����ڷ���ڣ���֧���һ�����\",");
					default: return null;
				}
			}
			else {	//δע���û�
				throw new FindBackPasswordException("\"email-error-msg\":\"�����䲻����\",");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new FindBackPasswordException("\"find-error-msg\":\"�һ�ʧ��\",");
		}
	}

	public void sendResetPasswordLink(Account account, String basePath) throws FindBackPasswordException {
		try {
			dao.update(account, AccUpdateMode.FINDPASSWD);
			//���������ʼ�
			String emailMsg = "&nbsp;&nbsp;��л��ʹ��MyMemonts, ������������ <p>" +
					"<a href='http://localhost:8080" + basePath + "/ResetLinkServlet" +
					"?resetCode=" + account.getActivationCode() + "&accountId=" +
					account.getAccountId() + "'>http://localhost:8080" + basePath +
					"/ResetLinkServlet" + "?resetCode=" + account.getActivationCode() +
					"&accountId=" + account.getAccountId() + "</a></p> ���������˻������롣" +
					"ͬʱΪ�����˻��İ�ȫ�����ӽ���30���Ӻ�ʧЧ�����Ǳ��˲������������˻���Ϣ�Ƿ�й¶�����벻Ҫ������������ת�����ˣ������ϵ���ƭ��";
			MailUtils.sendMail(account.getEmailAddr(), "��������", emailMsg);
		} catch (MessagingException | SQLException e) {
				e.printStackTrace();
				throw new FindBackPasswordException("\"find-error-msg\":\"�һ�ʧ��\",");
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
			throw new FindBackPasswordException("\\\"reset-error-msg\\\":\\\"����ʧ��\\\",");
		}
		
	}


	//��������
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
