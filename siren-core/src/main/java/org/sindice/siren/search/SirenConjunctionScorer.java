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
 * @author Renaud Delbru [ 27 Apr 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.apache.lucene.search.Weight;

/**
 * Scorer for conjunctions, sets of queries, within a cell. All the queries
 * are required.
 * <p> A cell is considered matching if all the queries match in the same cell.
 * The {@link #nextDoc()} method iterates over entities that contain one
 * or more matching cells. The {@link #nextPosition()} allows to iterate
 * over the cells within an entity.
 * <p> Code taken from {@link ConjunctionScorer} and adapted for the Siren use
 * case.
 **/
class SirenConjunctionScorer
extends SirenScorer {

  private final SirenPrimitiveScorer[] scorers;

  /** Flag to know if next or skipTo has been called */
  private boolean             firstTime = true;

  private boolean             more;

  private final float         coord;

  private int[]               lastNode;

  private int                 lastEntity   = -1;
  
  public SirenConjunctionScorer(final Weight weight,
                                final Collection<SirenPrimitiveScorer> scorers,
                                float coord)
  throws IOException {
    this(weight, scorers.toArray(new SirenPrimitiveScorer[scorers.size()]), coord);
  }

  public SirenConjunctionScorer(final Weight weight,
                                final SirenPrimitiveScorer[] scorers,
                                float coord)
  throws IOException {
    super(weight);
    this.scorers = scorers;
    this.coord = coord;
  }

  @Override
  public int docID() {
    return lastEntity;
  }

  /**
   * Position is invalid in high-level scorers. It will always return
   * {@link Integer.MAX_VALUE}.
   */
  @Override
  public int pos() {
    return Integer.MAX_VALUE;
  }

  @Override
  public int nextDoc()
  throws IOException {
    if (firstTime)
      return this.init(0);
    else if (more) {
      more = (scorers[(scorers.length - 1)].nextDoc() != NO_MORE_DOCS);
    }
    return this.doNext();
  }

  private boolean isBefore(int aDocID, int[] aNodes, int bDocID, int[] bNodes) {
    for (int i = 0; i < bNodes.length; i++) {
      int index = i;
      boolean res = aDocID == bDocID && aNodes[index] < bNodes[index];
      
      while (--index >= 0) {
        res = aNodes[index] == bNodes[index] && res;
      }
      if (res) return true;
    }
    return false;
  }
  
  /**
   * Perform a next without initial increment
   */
  private int doNext()
  throws IOException {
    int first = 0;
    SirenScorer lastScorer = scorers[scorers.length - 1];
    SirenScorer firstScorer = scorers[first];

    while (more &&
           (firstScorer.docID() < lastScorer.docID() ||
            isBefore(firstScorer.docID(), firstScorer.node(), lastScorer.docID(), lastScorer.node()))) {
      more = (firstScorer.advance(lastScorer.docID(), lastScorer.node()) != NO_MORE_DOCS);
      lastScorer = firstScorer;
      first = (first == (scorers.length - 1)) ? 0 : first + 1;
      firstScorer = scorers[first];
    }

    if (more) {
      lastEntity = lastScorer.docID();
      lastNode = lastScorer.node().clone();
      return lastEntity;
    }
    else {
      lastEntity = Integer.MAX_VALUE; // sentinel value
      lastNode = lastScorer.node().clone();
      return NO_MORE_DOCS;
    }
  }

  private boolean isBefore(int[] aNodes, int[] bNodes) {
    for (int i = 0; i < bNodes.length; i++) {
      int index = i;
      boolean res = aNodes[index] < bNodes[index];
      
      while (--index >= 0) {
        res = aNodes[index] == bNodes[index] && res;
      }
      if (res) return true;
    }
    return false;
  }
  
  @Override
  public int nextPosition() throws IOException {
    int first = 0;
    SirenScorer lastScorer = scorers[scorers.length - 1];
    SirenScorer firstScorer = scorers[first];

    if (lastScorer.nextPosition() == NO_MORE_POS) { // scan forward in last
      return NO_MORE_POS;
    }

    while (isBefore(firstScorer.node(), lastScorer.node())) { // scan forward in first
      do {
        if (firstScorer.nextPosition() == NO_MORE_POS)
          return NO_MORE_POS;
      } while (isBefore(firstScorer.node(), lastScorer.node()));
      lastScorer = firstScorer;
      first = (first == (scorers.length - 1)) ? 0 : first + 1;
      firstScorer = scorers[first];
    }
    // all equal: a match
    lastNode = firstScorer.node().clone();
    return -1; // position is invalid in this scorer, returns -1
  }

  @Override
  public int advance(final int entityID) throws IOException {
    if (firstTime)
      return this.init(entityID);
    else if (more) {
      more = (scorers[(scorers.length - 1)].advance(entityID) != NO_MORE_DOCS);
    }
    return this.doNext();
  }

  @Override
  public int advance(int docID, int[] nodes)
  throws IOException {
    if (firstTime)
      return this.init(docID); //TODO: should not skip to the right tuple in certain case
    else if (more) {
      more = (scorers[(scorers.length - 1)].advance(docID, nodes) != NO_MORE_DOCS);
    }
    return this.doNext();
  }

  // Note... most of this could be done in the constructor
  // thus skipping a check for firstTime per call to next() and skipTo()
  private int init(final int target)
  throws IOException {
    firstTime = false;
    more = scorers.length > 1;

    for (final SirenScorer scorer : scorers) {
      more = target == 0 ?
                          (scorer.nextDoc() != NO_MORE_DOCS)
                         :
                          (scorer.advance(target) != NO_MORE_DOCS);

      if (!more) return NO_MORE_DOCS;
    }

    // Sort the array the first time...
    // We don't need to sort the array in any future calls because we know
    // it will already start off sorted (all scorers on same doc).

    // note that this comparator is not consistent with equals!
    Arrays.sort(scorers, new Comparator<SirenScorer>() { // sort the array
        public int compare(final SirenScorer o1, final SirenScorer o2) {
          if (o1.docID() != o2.docID()) {
            return o1.docID() - o2.docID(); 
          }
          final int maxIndex = o1.node().length - 1;
          for (int i = 0; i < maxIndex; i++) {
            if (o1.node()[i] != o2.node()[i])
              return o1.node()[i] - o2.node()[i];
          }
          return o1.node()[maxIndex] - o2.node()[maxIndex];
        }
      });

    this.doNext();

    // If first-time skip distance is any predictor of
    // scorer sparseness, then we should always try to skip first on
    // those scorers.
    // Keep last scorer in it's last place (it will be the first
    // to be skipped on), but reverse all of the others so that
    // they will be skipped on in order of original high skip.
    final int end = (scorers.length - 1);
    for (int i = 0; i < (end >> 1); i++) {
      final SirenPrimitiveScorer tmp = scorers[i];
      scorers[i] = scorers[end - i - 1];
      scorers[end - i - 1] = tmp;
    }

    if (more) {
      return lastEntity;
    }
    else {
      return NO_MORE_DOCS;
    }
  }

  @Override
  public float score()
  throws IOException {
    float sum = 0.0f;
    for (final SirenPrimitiveScorer scorer : scorers) {
      sum += scorer.score();
    }
    return sum * coord;
  }

  @Override
  public String toString() {
    return "SirenConjunctionScorer(" + this.docID() + "," + Arrays.toString(lastNode) + ")";
  }

  @Override
  public int[] node() {
    return lastNode;
  }

}
