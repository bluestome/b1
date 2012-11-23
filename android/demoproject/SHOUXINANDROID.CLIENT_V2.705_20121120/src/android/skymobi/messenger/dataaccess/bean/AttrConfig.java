package android.skymobi.messenger.dataaccess.bean;

/**
 * 配置表的实体
 * 
 * */
public class AttrConfig implements IConfig{

	/** 属性名称 */
	private String attrName;
	/** 属性值 */
	private String attrVal;
	/** 最后更新的版本号，如果该属性适合所有版本，置为空 */
	private String version;
	/** 属性说明 */
	private String caption;
	/** 保留位 */
	private String reserve0;
	/** 保留位 */
	private String reserve1;
	/** 属性创建时间 */
	private long createTime;

	public AttrConfig() {
	}

	public AttrConfig(String attrName, String attrVal) {
		this.attrName = attrName;
		this.attrVal = attrVal;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public String getAttrVal() {
		return attrVal;
	}

	public void setAttrVal(String attrVal) {
		this.attrVal = attrVal;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getReserve0() {
		return reserve0;
	}

	public void setReserve0(String reserve0) {
		this.reserve0 = reserve0;
	}

	public String getReserve1() {
		return reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	@Override
	public String toString(){
		return "attrName:"+attrName+",attrVal:"+attrVal;
	}
}
