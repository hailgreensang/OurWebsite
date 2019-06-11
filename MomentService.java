package cn.mymoments.service;

import java.sql.SQLException;
import java.util.List;

import cn.mymoments.dao.DAOFactory;
import cn.mymoments.dao.MomentDAO;
import cn.mymoments.domain.Moment;
import cn.mymoments.exception.GetMomentException;
import cn.mymoments.exception.PublishException;

public class MomentService {
	private MomentDAO dao = DAOFactory.getMomentDAOInstance();

	/**
	 * ��ȡĳһ�û�ĳһ��̬ҳ��̬�б����
	 * 
	 * @param pageNo
	 * @param limit
	 * @param owner
	 * @return
	 * @throws GetMomentException
	 */
	public List<Moment> getMomentPage(int startNo, int limit, int owner) throws GetMomentException {
		try {
			System.out.println("owner moments sum " + dao.findMomentsByOwner(startNo, limit, owner).size());
			return dao.findMomentsByOwner(startNo, limit, owner);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new GetMomentException("��ȡʧ��");
		}
	}

	/**
	 * ��ȡȫ��ĳһ��̬ҳ��̬�б����
	 * 
	 * @param pageNo
	 * @param limit
	 * @return
	 * @throws GetMomentException
	 */
	public List<Moment> getMomentPage(int startNo, int limit) throws GetMomentException {
		try {
			System.out.println("getMomentPage");
			return dao.findAll(startNo, limit);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new GetMomentException("��ȡʧ��");
		}
	}

	/**
	 * ������̬����
	 * 
	 * @param moment
	 * @throws PublishException
	 */
	public void publish(Moment moment) throws PublishException {
		try {
			dao.addMoment(moment);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new PublishException("����ʧ��");
		}
	}

	/**
	 * ��ȡ��̬ҳ������
	 * 
	 * @param limit
	 * @param owner
	 * @return
	 * @throws GetMomentException
	 */
	public long countPages(int limit, int owner) throws GetMomentException {
		try {
			long cnt = dao.countMoments(owner);
			if (cnt == 0)
				throw new GetMomentException("û������");
			return (cnt - 1) / limit + 1;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new GetMomentException("��ȡʧ��");
		}
	}

	/**
	 * ��ȡ��̬ҳ������
	 * 
	 * @param limit
	 * @return
	 * @throws GetMomentException
	 */
	public long countPages(int limit) throws GetMomentException {
		return countPages(limit, -1);
	}

	public long countMomentsNum(int owner) throws GetMomentException {
		try {
			long cnt = dao.countMoments(owner);
			if (cnt == 0)
				throw new GetMomentException("û������");
			return cnt;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new GetMomentException("��ȡʧ��");
		}
	}

	public long countMomentsNum() throws GetMomentException {
		return countMomentsNum(-1);
	}

	public Integer getMaxMomentsId(int owner) throws GetMomentException {
		try {
			Integer cnt = dao.getMaxMomentsId(owner);
			if (cnt==null || cnt == 0)
				throw new GetMomentException("û������");
			return cnt;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new GetMomentException("��ȡʧ��");
		}
	}

	public long getMaxMomentsId() throws GetMomentException {
		return getMaxMomentsId(-1);
	}

	public int getMinListMomentId(int startNo, int limit, int owner) throws GetMomentException {
		List<Moment> list = getMomentPage(startNo, limit, owner);
		if (list.size() - 1 >= 0)
			return list.get(list.size() - 1).getMomentId();
		return -1;
	}

	public int getMinListMomentId(int startNo, int limit) throws GetMomentException {
		List<Moment> list = getMomentPage(startNo, limit);
		return list.get(list.size() - 1).getMomentId();
	}
	public void delMomentById(int id) throws SQLException {
		dao.delMomentById(id);
	}

	public List<Moment> findMomentsByContent(String content) throws GetMomentException {
		try {
			if(dao.findMomentByContent(content).isEmpty()) {
				return dao.findAllMoments();
			}
			else {
				return dao.findMomentByContent(content);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new GetMomentException("��ȡʧ��");
		}

	}
}
