public class Page {
	private int beginpage;
	private int endpage;
	private int beginlog;
	private int endlog;
	private int maxpage;//ҳ�����ֵ
	
	private int logblock;//һҳ��̬��
	private int pageblock;//�鳤��
	private int nowpage;//��ǰҳ��
	private int lognum;//�ܶ�̬��
	private String[] pages;
	public Page(int lognum, int logblock, int pageblock) {
		this.lognum = lognum;
		this.logblock = logblock;
		this.pageblock = pageblock;
		this.maxpage = (lognum / logblock);
		if(lognum % logblock != 0)
		{
			maxpage += 1;
		}
		this.beginlog = 1;
		this.beginpage = 1;
		this.endlog = 1 + logblock - 1;
		this.endpage = 1 + pageblock - 1;
		this.nowpage = 1;
		if(this.endpage > this.maxpage)
		{
			this.endpage = this.maxpage;
		}
		if(this.beginpage <= 0)
		{
			this.beginpage = 1;
		}
	}
	public int getMaxpage() {
		return maxpage;
	}

	public void setMaxpage(int maxpage) {
		this.maxpage = maxpage;
	}
	public int getBeginpage() {
		return beginpage;
	}

	public void setBeginpage(int beginpage) {
		this.beginpage = beginpage;
	}

	public int getEndpage() {
		return endpage;
	}

	public void setEndpage(int endpage) {
		this.endpage = endpage;
	}

	public int getBeginlog() {
		return beginlog;
	}

	public void setBeginlog(int beginlog) {
		this.beginlog = beginlog;
	}

	public int getEndlog() {
		return endlog;
	}

	public void setEndlog(int endlog) {
		this.endlog = endlog;
	}

	public int getLogblock() {
		return logblock;
	}

	public void setLogblock(int logblock) {
		this.logblock = logblock;
	}

	public int getPageblock() {
		return pageblock;
	}

	public void setPageblock(int pageblock) {
		this.pageblock = pageblock;
	}

	public int getNowpage() {
		return nowpage;
	}

	public void setNowpage(int nowpage) {
		this.nowpage = nowpage;
	}

	public int getLognum() {
		return lognum;
	}

	public void setLognum(int lognum) {
		this.lognum = lognum;
	}

	public void nextPage()
	{
		if(this.nowpage + 1 <= this.maxpage)
		{
			this.nowpage += 1;
			this.beginpage = nowpage - pageblock / 2;
			this.endpage = nowpage + pageblock / 2;

			
			if(this.beginpage <= 0)
			{
				this.beginpage = 1;
			}
			if(this.endpage - this.beginpage + 1 < pageblock && this.beginpage + pageblock - 1 <= this.maxpage)
			{
				this.endpage = this.beginpage + pageblock - 1;
			}
			if(this.endpage > this.maxpage)
			{
				this.endpage = this.maxpage;
			}
			this.beginlog += this.logblock;
			this.endlog += this.logblock;
		}

	}
	public void lastPage()
	{
		if(this.nowpage - 1 >= 1)
		{
			this.nowpage -= 1;
			this.beginpage = nowpage - pageblock /2;
			this.endpage = nowpage + pageblock / 2;

			if(this.endpage > this.maxpage)
			{
				this.endpage = this.maxpage;
			}
			if(this.endpage - this.beginpage + 1 < pageblock && this.endpage - pageblock + 1 >= 1)
			{
				this.beginpage = this.endpage - pageblock + 1;
			}
			if(this.beginpage <= 0)
			{
				this.beginpage = 1;
			}
			this.beginlog -= this.logblock;
			this.endlog -= this.logblock;
		}
	}
	//��ת��Nҳ
	public void gotoPage(int n)
	{
		
		if(n <= maxpage && n >= 1)
		{
			this.nowpage = n;
			this.beginpage = n - pageblock / 2;
			this.endpage = n + pageblock / 2;
			
			if(this.endpage > this.maxpage)
			{
				this.endpage = this.maxpage;
			}
			if(this.beginpage <= 0)
			{
				this.beginpage = 1;
			}
			this.beginlog = (n - 1) * logblock + 1;
			this.endlog = this.beginlog + this.logblock - 1;
		}
	}
	
}
