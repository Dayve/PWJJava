package testGen.model;

import java.util.ArrayList;
import java.util.List;

public class Week {
	@Override public String toString() {
		return "Week [pn=" + pn + ", wt=" + wt + ", sr=" + sr + ", cz=" + cz + ", pt=" + pt + ", sb=" + sb + ", nd="
				+ nd + "]";
	}

	private String pn, wt, sr, cz, pt, sb, nd;

	public Week(List<String> initializerList) throws IllegalArgumentException {
		super();

		if (initializerList.size() != 7) {
			this.pn = "~";
			this.wt = "~";
			this.sr = "~";
			this.cz = "~";
			this.pt = "~";
			this.sb = "~";
			this.nd = "~";

			throw new IllegalArgumentException();
		} else {
			this.pn = initializerList.get(0);
			this.wt = initializerList.get(1);
			this.sr = initializerList.get(2);
			this.cz = initializerList.get(3);
			this.pt = initializerList.get(4);
			this.sb = initializerList.get(5);
			this.nd = initializerList.get(6);
		}
	}

	public List<String> getValuesAsStringList() {
		List<String> returned = new ArrayList<String>();
		returned.add(pn);
		returned.add(wt);
		returned.add(sr);
		returned.add(cz);
		returned.add(pt);
		returned.add(sb);
		returned.add(nd);

		return returned;
	}

	public String getPn() {
		return pn;
	}

	public void setPn(String pn) {
		this.pn = pn;
	}

	public String getWt() {
		return wt;
	}

	public void setWt(String wt) {
		this.wt = wt;
	}

	public String getSr() {
		return sr;
	}

	public void setSr(String sr) {
		this.sr = sr;
	}

	public String getCz() {
		return cz;
	}

	public void setCz(String cz) {
		this.cz = cz;
	}

	public String getPt() {
		return pt;
	}

	public void setPt(String pt) {
		this.pt = pt;
	}

	public String getSb() {
		return sb;
	}

	public void setSb(String sb) {
		this.sb = sb;
	}

	public String getNd() {
		return nd;
	}

	public void setNd(String nd) {
		this.nd = nd;
	}
}
