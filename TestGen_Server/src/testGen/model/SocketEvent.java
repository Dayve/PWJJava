package testGen.model;

import java.io.Serializable;

public class SocketEvent implements Serializable {

	private static final long serialVersionUID = -1416783796467274992L;
	private String name;
	private Object[] data;

	public SocketEvent(String name, Object... data) {
		this.name = name;
		this.data = data;
	}

	@SuppressWarnings("unchecked") public <T> T getObject(Class<T> ofClass) {
		T obj = null;
		for (int i = 0; i < data.length; i++) {
			if (ofClass.isInstance(data[i])) {
				obj = (T) data[i];
			}
		}
		return obj;
	}

	public String getName() {
		return name;
	}
}
