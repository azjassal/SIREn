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
 * @author Renaud Delbru [ 10 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search;

import java.io.IOException;

/** A scorer that matches no entity at all. */
class NonMatchingScorer extends SirenPrimitiveScorer {

  public NonMatchingScorer() { super(null); } // no similarity used

  @Override
  public int docID() { throw new UnsupportedOperationException(); }

  @Override
  public int pos() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int nextDoc() throws IOException { return NO_MORE_DOCS; }

  @Override
  public int nextPosition()
  throws IOException {
    return NO_MORE_POS;
  }

  @Override
  public float score() { throw new UnsupportedOperationException(); }

  @Override
  public int advance(final int target) { return NO_MORE_DOCS; }

  @Override
  public int advance(int docID, int[] nodes)
  throws IOException {
    return NO_MORE_DOCS;
  }

  @Override
  public int[] node() {
    throw new UnsupportedOperationException();
  }

//  @Override
//  public Explanation explain(final int doc) {
//    final Explanation e = new Explanation();
//    e.setDescription("No entity matches.");
//    return e;
//  }

}


