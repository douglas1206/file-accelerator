package com.lwtx.wisdom.upload;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class UploadWisdomTrait {

	public static final String UPLOAD_SYNC_DIR = "wisdom_acc_upload_sync_wisdom_dir";

	public static final String TFS_SEPERATOR = "wisdom_sep";
	public static final String TFS_ID = "tfsId";
	public static final String RETURNTFS_ID = "returnTfsId";

	private static final Logger LOGGER = Logger.getLogger(UploadWisdomTrait.class);

	@Autowired
	private UploadRootSubDirTrait dirTrait;

	@PostConstruct
	private void pc() {
		dirTrait.setName(UPLOAD_SYNC_DIR);
		LOGGER.info(String.format("Using upload wisdom sync dir: %s", dirTrait.getPath()));
	}

	public boolean buildTask(String tfsId, String returnTfsId) {
		File taskFile = dirTrait.getChild(encodeName(tfsId, returnTfsId));
		if (taskFile.exists()) {
			return true;
		}

		try {
			taskFile.createNewFile();
			LOGGER.info(String.format("wisdom sync's task, realid to wisdom: %s", dirTrait.getPath()));
			return true;
		} catch (IOException e) {
			LOGGER.error("build task fail for", e);
			return false;
		}
	}

	public String encodeName(String tfsId, String returnTfsId) {
		return tfsId + TFS_SEPERATOR + returnTfsId;
	}

	public Map<String, String> decode(String name) {
		String[] ids = name.split(TFS_SEPERATOR);
		Map<String, String> map = new HashMap<String, String>();

		map.put(TFS_ID, ids[0]);
		map.put(RETURNTFS_ID, ids[1]);
		return map;
	}

	public String decodeTfsId(String name) {
		return decode(name).get(TFS_ID);
	}

	public String descodeReturnTfsId(String name) {
		return decode(name).get(RETURNTFS_ID);
	}

	public UploadRootSubDirTrait getDirTrait() {
		return dirTrait;
	}

}