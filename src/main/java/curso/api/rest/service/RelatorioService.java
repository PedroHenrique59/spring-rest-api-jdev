package curso.api.rest.service;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;

@Service
public class RelatorioService implements Serializable {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public byte[] gerarRelatorio(String nomeRelatorio, ServletContext servletContext) throws Exception {
        Connection connection = jdbcTemplate.getDataSource().getConnection();

        String caminhoJasper = servletContext.getRealPath("relatorios") + File.separator + nomeRelatorio + ".jasper";

        JasperPrint jasperPrint = JasperFillManager.fillReport(caminhoJasper, new HashMap<>(), connection);

        byte[] retorno = JasperExportManager.exportReportToPdf(jasperPrint);

        connection.close();

        return retorno;
    }
}
