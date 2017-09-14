package es.trueta.documentacion_repo.transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.util.transaction.TransactionListenerAdapter;

public class RelatedNodesTransactionListener extends TransactionListenerAdapter {

	public static final String KEY_RELATED_NODES = "keyRelatedNodes";
	
	
	private ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

	@Override
	public void beforeCommit(boolean readOnly) {
		List<Runnable> nodes = AlfrescoTransactionSupport.getResource(KEY_RELATED_NODES);
		if (nodes != null) {
			try {
				nodes.forEach(r -> threadPoolExecutor.execute(r));

				//TODO: Borrar
				// esperamos 5 segundos para que se ejecute todo sino pues ya
				// acabara cuando tenga que acabar
//				try {
//					threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}