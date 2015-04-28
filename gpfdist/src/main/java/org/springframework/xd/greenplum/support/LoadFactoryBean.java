package org.springframework.xd.greenplum.support;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.xd.greenplum.GreenplumLoad;
import org.springframework.xd.greenplum.dao.DefaultLoadService;

/**
 * FactoryBean for easy creation and configuration of {@link GreenplumLoad}
 * instances.
 *
 * @author Janne Valkealahti
 *
 */
public class LoadFactoryBean implements FactoryBean<GreenplumLoad>, InitializingBean, DisposableBean {

	private DataSource dataSource;

	private LoadConfiguration loadConfiguration;

	private JdbcTemplate jdbcTemplate;

	@Override
	public GreenplumLoad getObject() throws Exception {
		return new DefaultGreenplumLoad(loadConfiguration, new DefaultLoadService(jdbcTemplate));
	}

	@Override
	public Class<GreenplumLoad> getObjectType() {
		return GreenplumLoad.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws IOException {
		Assert.notNull(dataSource, "DataSource must not be null.");
		Assert.notNull(loadConfiguration, "Load configuration must not be null.");
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void destroy() {
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setLoadConfiguration(LoadConfiguration LoadConfiguration) {
		this.loadConfiguration = LoadConfiguration;
	}

}