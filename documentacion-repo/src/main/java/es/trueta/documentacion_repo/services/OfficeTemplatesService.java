package es.trueta.documentacion_repo.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import es.trueta.documentacion_repo.transactional.RelatedNodesTransactionListener;

public class OfficeTemplatesService {

	private NamespaceService namespaceService;
	private NodeService nodeService;
	private ContentService contentService;
	private MimetypeService mimetypeService;
	private CopyService copyService;
	private PermissionService permissionService;

	private TransactionService transactionService;

	private RelatedNodesTransactionListener transactionListener;

	public NodeRef generaDocumentoPlantilla(NodeRef node, NodeRef plantilla) throws Exception {

		Map<QName, Serializable> nodeProperties = nodeService.getProperties(node);

		ContentReader readerPlantilla = contentService.getReader(plantilla, ContentModel.PROP_CONTENT);

		// Crear nodo con el resultado del word
		NodeRef parentNodo = nodeService.getPrimaryParent(node).getParentRef();
		NodeRef wordModificadoNode = nodeService.createNode(parentNodo, ContentModel.ASSOC_CONTAINS,
				QName.createQName(ContentModel.PROP_CONTENT.getNamespaceURI(),
						QName.createValidLocalName(
								System.currentTimeMillis() + (String) nodeProperties.get(ContentModel.PROP_NAME))),
				ContentModel.TYPE_CONTENT).getChildRef();

		ContentWriter writerWordModified = contentService.getWriter(wordModificadoNode, ContentModel.PROP_CONTENT,
				true);
		writerWordModified.setMimetype(readerPlantilla.getMimetype());
		writerWordModified.setEncoding(readerPlantilla.getEncoding());

		// Crear word en base plantilla y setearlo
		wordDocProcessor(readerPlantilla.getContentInputStream(), writerWordModified.getContentOutputStream(),
				nodeProperties);

		// Crear nodo para pdf plantilla
		NodeRef pdfPlantilla = nodeService.createNode(parentNodo, ContentModel.ASSOC_CONTAINS,
				QName.createQName(ContentModel.PROP_CONTENT.getNamespaceURI(),
						QName.createValidLocalName(System.currentTimeMillis() + ".pdf")),
				ContentModel.TYPE_CONTENT).getChildRef();

		ContentWriter pdfPlantillaWriter = contentService.getWriter(pdfPlantilla, ContentModel.PROP_CONTENT, true);
		pdfPlantillaWriter.setMimetype(MimetypeMap.MIMETYPE_PDF);
		pdfPlantillaWriter.setEncoding(StandardCharsets.UTF_8.name());

		contentService.transform(contentService.getReader(wordModificadoNode, ContentModel.PROP_CONTENT),
				pdfPlantillaWriter);

		// Crear nodo para pdf nodo
		NodeRef pdfNode = nodeService.createNode(parentNodo, ContentModel.ASSOC_CONTAINS,
				QName.createQName(ContentModel.PROP_CONTENT.getNamespaceURI(),
						QName.createValidLocalName(System.currentTimeMillis() + ".pdf")),
				ContentModel.TYPE_CONTENT).getChildRef();

		ContentWriter writerPdfNodo = contentService.getWriter(pdfNode, ContentModel.PROP_CONTENT, true);
		writerPdfNodo.setMimetype(MimetypeMap.MIMETYPE_PDF);
		writerPdfNodo.setEncoding(StandardCharsets.UTF_8.name());
		contentService.transform(contentService.getReader(node, ContentModel.PROP_CONTENT), writerPdfNodo);

		// Nodo final
		String newName = TransformActionExecuter.transformName(mimetypeService,
				(String) nodeProperties.get(ContentModel.PROP_NAME), MimetypeMap.MIMETYPE_PDF, true);
		NodeRef nodoResult = copyService.copyAndRename(node, parentNodo, ContentModel.ASSOC_CONTAINS,
				QName.createQName(ContentModel.PROP_CONTENT.getNamespaceURI(), QName.createValidLocalName(newName)),
				false);

		// Si no existe un documento con el mismo nombre lo renombramos
		// Sino lo dejamos como esta para que no falle por nombre duplicado y solo
		// cambiamos la extension
		if (nodeService.getChildrenByName(parentNodo, ContentModel.ASSOC_CONTAINS, Arrays.asList(newName)).isEmpty()) {
			nodeService.setProperty(nodoResult, ContentModel.PROP_NAME, newName);
		} else {
			String name = (String) nodeService.getProperty(nodoResult, ContentModel.PROP_NAME);
			name = name.substring(0, name.lastIndexOf(".")) + ".pdf";
			nodeService.setProperty(nodoResult, ContentModel.PROP_NAME, name);
		}

		nodeService.removeProperty(nodoResult, ContentModel.PROP_CONTENT);

		ContentWriter writerFinal = contentService.getWriter(nodoResult, ContentModel.PROP_CONTENT, true);
		writerFinal.setMimetype(MimetypeMap.MIMETYPE_PDF);
		writerFinal.setEncoding(StandardCharsets.UTF_8.name());

		joinPdfs(
				Arrays.asList(contentService.getReader(pdfPlantilla, ContentModel.PROP_CONTENT).getContentInputStream(),
						contentService.getReader(pdfNode, ContentModel.PROP_CONTENT).getContentInputStream()),
				writerFinal.getContentOutputStream());

		borrarNodosSegundoPlano(wordModificadoNode, pdfPlantilla, pdfNode);

		return nodoResult;
	}

	private void borrarNodosSegundoPlano(NodeRef... nodes) {

		RetryingTransactionHelper transactionHelper = transactionService.getRetryingTransactionHelper();

		hideNodes(nodes);

		AlfrescoTransactionSupport.bindListener(transactionListener);
		List<Runnable> actionsRelated = AlfrescoTransactionSupport
				.getResource(RelatedNodesTransactionListener.KEY_RELATED_NODES);

		if (actionsRelated == null) {
			actionsRelated = new ArrayList<>();
		}

		actionsRelated.add(() -> {
			transactionHelper.doInTransaction(() -> {
				AuthenticationUtil.runAsSystem(() -> {
					Arrays.asList(nodes).forEach(n -> {
						if (nodeService.exists(n)) {
							nodeService.deleteNode(n);
						}
					});
					return null;
				});
				return null;
			}, false, true);
		});

		AlfrescoTransactionSupport.bindResource(RelatedNodesTransactionListener.KEY_RELATED_NODES, actionsRelated);

	}

	private void hideNodes(NodeRef... nodes) {
		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_VISIBILITY_MASK, Visibility.NotVisible.getMask());
        Arrays.asList(nodes).forEach(n -> nodeService.addAspect(n, ContentModel.ASPECT_HIDDEN, props));
	}

	private void joinPdfs(List<InputStream> inputs, OutputStream out) throws Exception {
		PDFMergerUtility ut = new PDFMergerUtility();
		for (InputStream input : inputs) {
			ut.addSource(input);
		}
		ut.setDestinationStream(out);
		ut.mergeDocuments();
		out.close();
	}

	private void wordDocProcessor(InputStream in, OutputStream out, Map<QName, Serializable> properties)
			throws Exception {
		final XWPFDocument doc = new XWPFDocument(in);
		properties.forEach((k, v) -> replace(doc, getKey(k), getValue(v)));
		doc.write(out);
	}

	private String getValue(Serializable value) {
		if (value == null) {
			return "";
		}
		return value.toString();
	}

	private String getKey(QName key) {
		return "${" + key.toPrefixString(namespaceService) + "}";
	}

	private void replace(XWPFDocument doc, String pattern, String value) {

		for (XWPFParagraph p : doc.getParagraphs()) {
			List<XWPFRun> runs = p.getRuns();
			if (runs != null) {
				for (int i = 0; i < runs.size(); i++) {
					XWPFRun r = runs.get(i);
					String text = r.getText(0);
					if (text != null) {
						if (text.contains(pattern)) {
							text = text.replace(pattern, value);
							r.setText(text, 0);
						} else if (pattern.startsWith(text)) {
							// recorremos los runs siguientes por si estan troceados
							int x = i;
							while (pattern.startsWith(text) && x < runs.size() - 1) {
								text += runs.get(++x).getText(0);
							}

							if (text.contains(pattern)) {
								text = text.replace(pattern, value);
								r.setText(text, 0);
								while (++i <= x) {
									p.removeRun(i);
									i--;
									x--;
								}
							}
						}
					}
				}
			}
		}

		for (XWPFTable tbl : doc.getTables()) {
			for (XWPFTableRow row : tbl.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph p : cell.getParagraphs()) {
						for (XWPFRun r : p.getRuns()) {
							String text = r.getText(0);
							if (text.contains(pattern)) {
								text = text.replace(pattern, value);
								r.setText(text);
							}
						}
					}
				}
			}
		}
	}

	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public MimetypeService getMimetypeService() {
		return mimetypeService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public CopyService getCopyService() {
		return copyService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public RelatedNodesTransactionListener getTransactionListener() {
		return transactionListener;
	}

	public void setTransactionListener(RelatedNodesTransactionListener transactionListener) {
		this.transactionListener = transactionListener;
	}

	public TransactionService getTransactionService() {
		return transactionService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

}
