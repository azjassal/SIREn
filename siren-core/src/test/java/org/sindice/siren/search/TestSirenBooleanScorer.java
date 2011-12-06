/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved.
 *
 * Project and contact information: http://www.siren.sindice.com/
 *
 * This file is part of the SIREn project.
 *
 * SIREn is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SIREn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with SIREn. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @project siren
 * @author Renaud Delbru [ 8 May 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.search.DocIdSetIterator;
import org.junit.Test;

public class TestSirenBooleanScorer
extends AbstractTestSirenScorer {

  @Test
  public void testNextReq()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . "});

    final SirenBooleanScorer scorer = this.getBooleanScorer(
      new String[] {"aaa", "bbb"},
      null, null);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(0, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextReqWithConstraints()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenCellScorer scorer = this.getCellScorer(0, 0,
      new String[] {"aaa", "bbb"},
      null, null);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(0, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextReqExcl()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenBooleanScorer scorer = this.getBooleanScorer(
      new String[] {"aaa"}, null, new String[] {"ccc"});

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextReqExclWithConstraints()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenCellScorer scorer = this.getCellScorer(1, 1,
      new String[] {"aaa"}, null, new String[] {"ccc"});

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextOpt()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc ddd\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenBooleanScorer scorer = this.getBooleanScorer(null,
      new String[] {"aaa", "ddd"}, null);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(0, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(0, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextOptWithConstraints()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc ddd\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenCellScorer scorer = this.getCellScorer(1, 1, null,
      new String[] {"aaa", "ddd"}, null);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(0, scorer.docID());
    assertEquals(1, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextReqOpt()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenBooleanScorer scorer = this.getBooleanScorer(new String[] {"aaa"},
      new String[] {"bbb"}, null);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextOptExcl()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenBooleanScorer scorer = this.getBooleanScorer(null,
      new String[] {"bbb"}, new String[] {"ccc"});

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Test
  public void testNextReqOptExcl()
  throws Exception {
    _helper.addDocumentsWithIterator(new String[] { "\"aaa bbb\" \"aaa ccc\" . \"aaa bbb ccc\" \"bbb ccc\" . ",
                                                    "\"aaa\" \"aaa bbb\" . " });

    final SirenBooleanScorer scorer = this.getBooleanScorer(new String[] {"aaa"},
      new String[] {"bbb"}, new String[] {"ccc"});

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(0, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertFalse(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(0, scorer.node()[1]);

    assertFalse(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);
    assertEquals(1, scorer.docID());
    assertEquals(0, scorer.node()[0]);
    assertEquals(1, scorer.node()[1]);

    assertTrue(scorer.nextPosition() == NodIdSetIterator.NO_MORE_POS);

    assertTrue(scorer.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
  }

  @Override
  protected void assertTo(final AssertFunctor functor, final String[] input,
                          final String[] terms, final int[][] deweyPath)
  throws Exception {}


}
