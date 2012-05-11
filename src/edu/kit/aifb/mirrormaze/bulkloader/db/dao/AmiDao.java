package edu.kit.aifb.mirrormaze.bulkloader.db.dao;

import edu.kit.aifb.mirrormaze.bulkloader.db.model.AmiModel;

public interface AmiDao extends Dao<AmiModel, String> {

	public AmiModel create(String repository, String imageId,
			String imageLocation, String imageOwnerAlias, String ownerId,
			String name, String description, String architecture,
			String platform, String imageType);

}