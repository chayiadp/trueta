/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package es.trueta.documentacion_repo.officeTransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Makes use of the {@link http://sourceforge.net/projects/joott/JOOConverter} library to perform 
 *  OpenOffice-driven conversions.
 * This requires that OpenOffice be running, but delivers a wider range of transformations
 *  than Tika is able to (Tika just translates into Text, HTML and XML)
 * 
 * @author Derek Hulley
 */
public class OpenOfficeContentTransformerWorker extends org.alfresco.repo.content.transform.OpenOfficeContentTransformerWorker
{
    private static Log logger = LogFactory.getLog(OpenOfficeContentTransformerWorker.class);

    @Override
    protected String getTempFilePrefix(){
        return "OOT";
    }
    
    
}
