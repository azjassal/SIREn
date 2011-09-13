/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @project siren
 * @author Renaud Delbru [ 21 Jan 2011 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2010 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.qparser.ntriple.query.builders;

import org.apache.commons.lang.NotImplementedException;
import org.apache.lucene.queryParser.core.QueryNodeException;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.standard.nodes.RangeQueryNode;
import org.apache.lucene.search.TermRangeQuery;

/**
 * Builds a {@link TermRangeQuery} object from a {@link RangeQueryNode} object.
 */
public class RangeQueryNodeBuilder implements ResourceQueryBuilder {

  public RangeQueryNodeBuilder() {
    // empty constructor
  }

  public TermRangeQuery build(QueryNode queryNode) throws QueryNodeException {
    throw new NotImplementedException("TermRange queries not supported yet");
    
//TODO: To implement when SIRen will support termrange queries    
//    RangeQueryNode rangeNode = (RangeQueryNode) queryNode;
//    ParametricQueryNode upper = rangeNode.getUpperBound();
//    ParametricQueryNode lower = rangeNode.getLowerBound();
//
//    boolean lowerInclusive = false;
//    boolean upperInclusive = false;
//
//    if (upper.getOperator() == CompareOperator.LE) {
//      upperInclusive = true;
//    }
//
//    if (lower.getOperator() == CompareOperator.GE) {
//      lowerInclusive = true;
//    }
//
//    String field = rangeNode.getField().toString();
//
//    TermRangeQuery rangeQuery = new TermRangeQuery(field, lower
//        .getTextAsString(), upper.getTextAsString(), lowerInclusive,
//        upperInclusive, rangeNode.getCollator());
//    
//    MultiTermQuery.RewriteMethod method = (MultiTermQuery.RewriteMethod)queryNode.getTag(MultiTermRewriteMethodAttribute.TAG_ID);
//    if (method != null) {
//      rangeQuery.setRewriteMethod(method);
//    }
//
//    return rangeQuery;

  }

}
