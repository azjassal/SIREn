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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Weight;
import org.sindice.siren.util.ScorerCellQueue;

/**
 * A Scorer for OR like queries within a cell, counterpart of
 * {@link SirenConjunctionScorer}.
 *
 * <p> Code taken from {@link DisjunctionSumScorer} and adapted for the Siren
 * use case.
 */
class SirenDisjunctionScorer
extends SirenScorer {

  /** The number of subscorers. */
  private final int                          nrScorers;

  /** The scorers. */
  protected final Collection<SirenPrimitiveScorer> scorers;

  /**
   * The scorerDocQueue contains all subscorers ordered by their current docID(),
   * with the minimum at the top. <br>
   * The scorerDocQueue is initialized the first time nextDoc() or advance() is
   * called. <br>
   * An exhausted scorer is immediately removed from the scorerDocQueue. <br>
   * If less than the minimumNrMatchers scorers remain in the scorerDocQueue
   * nextDoc() and advance() return false.
   * <p>
   * After each to call to nextDoc() or advance() <code>currentScore</code> is
   * the total score of the current matching doc, <code>nrMatchers</code> is the
   * number of matching scorers, and all scorers are after the matching doc, or
   * are exhausted.
   */
  private ScorerCellQueue                    scorerCellQueue = null;

  /** used to avoid size() method calls on scorerDocQueue */
  private int                                queueSize      = -1;

//  /** The dataset that currently matches. */
//  private final int                          dataset     = -1;

  /** The entity that currently matches. */
  private int                                docID     = -1;

  private int[]                              nodes;
  
//  /** The tuple that currently matches. */
//  private int                                tuple      = -1;
//
//  /** The cell that currently matches. */
//  private int                                cell       = -1;

  /** The number of subscorers that provide the current match. */
  protected int                              nrMatchers     = -1;

  private float                              currentScore   = Float.NaN;

  /**
   * Construct a <code>DisjunctionScorer</code>.
   *
   * @param subScorers
   *          A collection of at least two primitives scorers.
   */
  public SirenDisjunctionScorer(final Weight weight,
                                final Collection<SirenPrimitiveScorer> scorers) {
    super(weight);
    nrScorers = scorers.size();
    if (nrScorers <= 1) {
      throw new IllegalArgumentException("There must be at least 2 subScorers");
    }
    this.scorers = scorers;
  }

  /**
   * Construct a <code>DisjunctionScorer</code>.
   *
   * @param scorers
   *          An array of at least two primitive scorers.
   */
  public SirenDisjunctionScorer(final Weight weight,
                                final SirenPrimitiveScorer[] scorers) {
    this(weight, Arrays.asList(scorers));
  }

  /**
   * Called the first time next() or skipTo() is called to initialize
   * <code>scorerCellQueue</code>.
   */
  private void initScorerCellQueue()
  throws IOException {
    final Iterator<SirenPrimitiveScorer> si = scorers.iterator();
    scorerCellQueue = new ScorerCellQueue(nrScorers);
    queueSize = 0;
    while (si.hasNext()) {
      final SirenPrimitiveScorer se = si.next();
      if (se.nextDocument() != NO_MORE_DOCS) { // entity(), tuple() and cell () method will be used in scorerDocQueue.
        if (scorerCellQueue.insert(se)) {
          queueSize++;
        }
      }
      nodes = se.node(); // TODO: in order to initialize the local node info. THis should be changed when we will use IntsRef
    }
  }

  /**
   * Scores and collects all matching documents.
   *
   * @param hc
   *          The collector to which all matching documents are passed through
   *          {@link HitCollector#collect(int, float)}. <br>
   *          When this method is used the {@link #explain(int)} method should
   *          not be used.
   */
  @Override
  public void score(final Collector collector) throws IOException {
    collector.setScorer(this);
    while (this.nextDocument() != NO_MORE_DOCS) {
      collector.collect(docID);
    }
  }

  /**
   * Expert: Collects matching documents in a range. Hook for optimization. Note
   * that {@link #next()} must be called once before this method is called for
   * the first time.
   *
   * @param hc
   *          The collector to which all matching documents are passed through
   *          {@link HitCollector#collect(int, float)}.
   * @param max
   *          Do not score documents past this.
   * @return true if more matching documents may remain.
   */
  @Override
  public boolean score(final Collector collector, final int max, final int firstDocID)
  throws IOException {
    // firstDocID is ignored since nextDoc() sets 'currentDoc'
    collector.setScorer(this);
    while (docID < max) {
      collector.collect(docID);
      if (this.nextDocument() == NO_MORE_DOCS) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int nextDocument() throws IOException {
    if (scorerCellQueue == null) {
      this.initScorerCellQueue();
      if ((nrMatchers = scorerCellQueue.nrMatches()) > 0) {
        docID = scorerCellQueue.topEntity();
        this.nextPosition(); // advance to the first position [SRN-24]
        return docID;
      }
      return NO_MORE_DOCS;
    }
    return this.advanceAfterCurrent();
  }

  private boolean isEqualToSentinel(int[] nodes) {
    for (int i : nodes) {
      if (i == Integer.MAX_VALUE) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Advance to the next position.<br>
   * Set the cell and tuple information.<br>
   * Iterate over the queue, and count how many matchers there are. Increment
   * the score consequently.
   */
  @Override
  public int nextPosition() throws IOException {
    // if tuple or cell have been set to sentinel value, there is no more position
    if (isEqualToSentinel(scorerCellQueue.topNodes())) {
      return NO_MORE_POS;
    }
    this.nodes = scorerCellQueue.topNodes().clone();
    currentScore = 0;
    nrMatchers = 0;
    // Count how many matchers there are, and increment current score
    while (scorerCellQueue.topEntity() == docID &&
           Arrays.equals(scorerCellQueue.topNodes(), this.nodes)) { // while top is a match, advance
      currentScore += scorerCellQueue.topScore();
      if (scorerCellQueue.topIncMatchers()) nrMatchers++;
      if (!scorerCellQueue.topNextPositionAndAdjust()) {
        return 0; // stop, no more position. position is invalid in this scorer,
                  // return 0.
                  // All positions in the queue are consumed. If nextPosition
                  // is called another time, we will return NO_MORE_POS.
      }
    }
    return 0; // position is invalid in this scorer, return 0.
  }

  /**
   * Advance all subscorers after the current document determined by the top of
   * the <code>scorerCellQueue</code> if all the subscorers are not exhausted. <br>
   * At least the top scorer with the minimum entity number will be advanced.
   *
   * @return true iff there is a match. <br>
   *         In case there is a match, </code>entity</code>,
   *         </code>currentScore</code>, and </code>nrMatchers</code>
   *         describe the match.
   */
  protected int advanceAfterCurrent()
  throws IOException {
    if (scorerCellQueue.size() > 0) {
      if ((queueSize -= scorerCellQueue.nextAndAdjustElsePop()) == 0) {
        return NO_MORE_DOCS;
      }

      docID = scorerCellQueue.topEntity();
      this.nextPosition(); // advance to the first position [SRN-24]
      return docID;
    }
    return NO_MORE_DOCS;
  }

  /**
   * Returns the score of the current document matching the query. Initially
   * invalid, until {@link #next()} is called the first time.
   */
  @Override
  public float score()
  throws IOException {
    return currentScore;
  }

  /**
   * Returns the number of subscorers matching the current document. Initially
   * invalid, until {@link #next()} is called the first time.
   */
  public int nrMatchers() {
    return nrMatchers;
  }

  /**
   * Skips to the first match (including the current) whose document number is
   * greater than or equal to a given target. <br>
   * When this method is used the {@link #explain(int)} method should not be
   * used. <br>
   * The implementation uses the skipTo() method on the subscorers.
   *
   * @param entityID
   *          The target entity number.
   * @return true iff there is such a match.
   */
  @Override
  public int skipTo(final int entityID) throws IOException {
    if (scorerCellQueue == null) {
      this.initScorerCellQueue();
    }
    if (entityID <= docID) {
      return docID;
    }
    while (queueSize > 0) {
      if (scorerCellQueue.topEntity() >= entityID) {
        docID = scorerCellQueue.topEntity();
        this.nextPosition(); // advance to the first position [SRN-24]
        return docID;
      }
      else if (!scorerCellQueue.topSkipToAndAdjustElsePop(entityID)) {
        if (--queueSize == 0) {
          return NO_MORE_DOCS;
        }
      }
    }
    return NO_MORE_DOCS;
  }

  /**
   * Returns true if the nodes passed in arguments are before the curNode node,
   * and the document is still the same as curDocID.
   * @param docID
   * @param nodes
   * @return
   */
  private boolean isBeforeOrEquals(int curDocID, int[] curNodes, int docID, int[] nodes) {
    for (int i = 0; i < nodes.length; i++) {
      int index = i;
      boolean res = docID == curDocID && nodes[index] <= curNodes[index];
      
      while (--index >= 0) {
        res = nodes[index] == curNodes[index] && res;
      }
      if (res) return true;
    }
    return false;
  }
  
  /**
   * Returns true if the nodes passed in arguments are before the curNode node,
   * and the document is still the same as curDocID.
   * Perform a check on the curNode against the sentinel value.
   * @param docID
   * @param nodes
   * @return
   */
  private boolean isBeforeOrEqualsAndCheck(int curDocID, int[] curNodes, int docID, int[] nodes) {
    for (int i = 0; i < nodes.length; i++) {
      int index = i;
      boolean res = docID == curDocID && curNodes[index] != Integer.MAX_VALUE && nodes[index] <= curNodes[index];
      
      while (--index >= 0) {
        res = nodes[index] == curNodes[index] && res;
      }
      if (res) return true;
    }
    return false;
  }
  
  /**
   * Skips to the first match (including the current) whose entity and nodes
   * numbers (e.g., tuple and cell) are greater than or equal to the given targets.
   * <br>
   * When this method is used the {@link #explain(int)} method should not be
   * used. <br>
   * The implementation uses the skipTo() method on the subscorers.
   *
   * @param entityID
   *          The target entity number.
   * @param nodes
   *          The target nodes numbers (the dewey path).
   * @return true iff there is such a match.
   */
  @Override
  public int skipTo(final int entityID, final int[] nodes)
  throws IOException {
    if (scorerCellQueue == null) {
      this.initScorerCellQueue();
    }
    if (entityID < docID || isBeforeOrEquals(docID, node(), entityID, nodes)) {
      return docID;
    }
    while (queueSize > 0) {
      if (scorerCellQueue.topEntity() > entityID ||
      isBeforeOrEqualsAndCheck(scorerCellQueue.topEntity(), scorerCellQueue.topNodes(), entityID, nodes)) {
        docID = scorerCellQueue.topEntity();
        this.nextPosition();
        return docID;
      }
      else if (!scorerCellQueue.topSkipToAndAdjustElsePop(entityID, nodes)) {
        if (--queueSize == 0) {
          return NO_MORE_DOCS;
        }
      }
    }
    return NO_MORE_DOCS;
  }

  @Override
  public int doc() {
    return docID;
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
  public int[] node() {
    return nodes;
  }
  
  @Override
  public String toString() {
    return "SirenDisjunctionScorer(" + this.doc() + "," + Arrays.toString(node()) + ")";
  }

}
